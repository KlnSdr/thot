package thot.server;

import thot.buckets.service.BucketService;
import thot.common.command.Command;
import thot.common.command.CommandType;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.delete.DeleteHandler;
import thot.server.handler.read.ReadHandler;
import thot.server.handler.write.WriteHandler;
import thot.util.logging.LogLevel;
import thot.util.logging.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int PORT = 12903;
    private static final int THREADS = 10;
    private static final String version = "0.0.1";
    private static final Logger LOGGER = new Logger(Server.class);
    private final Date startTime;
    private ServerSocket server;
    private ExecutorService threadPool;
    private boolean isRunning = false;

    private Server(int port, int threadCount) {
        startTime = new Date();
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            LOGGER.trace(e);
            System.exit(1);
            return;
        }
        threadPool = Executors.newFixedThreadPool(threadCount);
        LOGGER.info("Server initialized on port " + port + " with " + threadCount + " threads.");
        start();
    }

    public static void startApplication() {
        printBanner();

        System.out.println();

        setLogLevel("DEBUG");
        BucketService.getInstance();
        new Server(PORT, THREADS);
    }

    private static void setLogLevel(String logLevelString) {
        LogLevel logLevel = LogLevel.DEBUG;
        try {
            logLevel = LogLevel.valueOf(logLevelString.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("invalid log level: " + logLevelString + ", using DEBUG");
        }
        Logger.setMaxLogLevel(logLevel);
    }

    private static void printBanner() {
        System.out.println("######## ##     ##  #######  ########");
        System.out.println("   ##    ##     ## ##     ##    ##");
        System.out.println("   ##    ##     ## ##     ##    ##");
        System.out.println("   ##    ######### ##     ##    ##");
        System.out.println("   ##    ##     ## ##     ##    ##");
        System.out.println("   ##    ##     ## ##     ##    ##");
        System.out.println("   ##    ##     ##  #######     ##");

        System.out.println("v" + version);
        System.out.println("initializing...");
        System.out.println();
    }

    private void start() {
        LOGGER.info("ready after " + (new Date().getTime() - startTime.getTime()) + "ms");
        LOGGER.info("Server started...");
        isRunning = true;
        registerStopHandler();
        acceptConnections();
    }

    private void acceptConnections() {
        while (isRunning) {
            try {
                Socket client = server.accept();
                threadPool.execute(() -> {
                    try {
                        handleConnection(client);
                    } catch (IOException | ClassNotFoundException e) {
                        LOGGER.trace(e);
                    } finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            LOGGER.trace(e);
                        }
                    }
                });
            } catch (IOException e) {
                LOGGER.trace(e);
            }
        }
    }

    private void handleConnection(Socket client) throws IOException, ClassNotFoundException {
        ObjectInputStream istream = new ObjectInputStream(client.getInputStream());

        Command command = (Command) istream.readObject();
        CommandType commandType = command.getCommandType();

        Response response;

        if (commandType == CommandType.READ) {
            response = new ReadHandler().handle(command);
        } else if (commandType == CommandType.WRITE) {
            response = new WriteHandler().handle(command);
        } else if (commandType == CommandType.DELETE) {
            response = new DeleteHandler().handle(command);
        } else {
            response = unknownCommandType(command);
        }

        ObjectOutputStream ostream = new ObjectOutputStream(client.getOutputStream());


        ostream.writeObject(response);
        ostream.flush();

        istream.close();
        ostream.close();

        client.close();
    }

    /**
     * Stops the server
     */
    private void stop() {
        isRunning = false;
        LOGGER.info("Server stopping...");
        threadPool.shutdown();
        try {
            if (threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.info("all tasks finished");
            } else {
                LOGGER.info("forcing shutdown");
                threadPool.shutdownNow();
            }
            server.close();
            LOGGER.info("Server stopped.");
        } catch (IOException | InterruptedException e) {
            LOGGER.trace(e);
        }
    }

    private Response unknownCommandType(Command command) {
        Response res = new Response();
        res.setResponseType(ResponseType.ERROR);
        res.setError("unknown command type: " + command.getCommandType());
        return res;
    }

    private void registerStopHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
}
