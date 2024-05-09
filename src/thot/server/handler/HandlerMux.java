package thot.server.handler;

import dobby.io.PureRequestHandler;
import dobby.util.logging.Logger;
import thot.common.command.Command;
import thot.common.command.CommandType;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.delete.DeleteHandler;
import thot.server.handler.keys.KeysHandler;
import thot.server.handler.names.BucketNameHandler;
import thot.server.handler.read.ReadHandler;
import thot.server.handler.write.WriteHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class HandlerMux implements PureRequestHandler {
    private static final Logger LOGGER = new Logger(HandlerMux.class);
    @Override
    public void onRequest(Socket socket) throws IOException {
        ObjectInputStream istream = new ObjectInputStream(socket.getInputStream());

        Command command;
        try {
            command = (Command) istream.readObject();
        } catch (ClassNotFoundException e) {
            LOGGER.trace(e);
            return;
        }
        CommandType commandType = command.getCommandType();

        Response response;

        switch (commandType) {
            case READ:
                response = new ReadHandler().handle(command);
                break;
            case WRITE:
                response = new WriteHandler().handle(command);
                break;
            case DELETE:
                response = new DeleteHandler().handle(command);
                break;
            case KEYS:
                response = new KeysHandler().handle(command);
                break;
            case BUCKETS:
                response = new BucketNameHandler().handle(command);
                break;
            default:
                response = unknownCommandType(command);
        }

        ObjectOutputStream ostream = new ObjectOutputStream(socket.getOutputStream());

        ostream.writeObject(response);
        ostream.flush();
    }

    private Response unknownCommandType(Command command) {
        Response res = new Response();
        res.setResponseType(ResponseType.ERROR);
        res.setError("unknown command type: " + command.getCommandType());
        return res;
    }
}
