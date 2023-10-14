package thot.connector;

import thot.common.command.Command;
import thot.common.command.CommandType;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.delete.DeletePayload;
import thot.server.handler.read.ReadPayload;
import thot.server.handler.write.WritePayload;
import thot.util.logging.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class Connector {
    private static final Logger LOGGER = new Logger(Connector.class);

    public static void main(String[] args) {
        String val1 = read("temperatureValues", "2023-01-01", String.class);
        LOGGER.info("read empty: " + (val1 == null));
        LOGGER.info("read: " + val1);
        boolean didWrite = write("temperatureValues", "2023-01-01", "25.0");
        LOGGER.info("write: " + didWrite);
        String val2 = read("temperatureValues", "2023-01-01", String.class);
        LOGGER.info("read: " + val2);
//        boolean didDelete = delete("temperatureValues", "2023-01-01");
//        LOGGER.info("delete: " + didDelete);
//        String val3 = read("temperatureValues", "2023-01-01", String.class);
//        LOGGER.info("read empty: " + (val3 == null));

    }

    private static Response sendRequest(Command command) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("localhost", 12903);

        ObjectOutputStream ostream = new ObjectOutputStream(socket.getOutputStream());

        ostream.writeObject(command);
        ostream.flush();

        ObjectInputStream istream = new ObjectInputStream(socket.getInputStream());

        Response response = (Response) istream.readObject();

        ostream.close();
        istream.close();
        socket.close();

        return response;
    }

    public static boolean write(String bucketName, String key, Serializable value) {
        try {
            Command command = new Command(CommandType.WRITE, bucketName, new WritePayload(key, value));
            Response response = sendRequest(command);

            return response.getResponseType() == ResponseType.SUCCESS;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.trace(e);
            return false;
        }
    }

    public static <T> T read(String bucketName, String key, Class<? extends T> typeClass) {
        try {
            Command command = new Command(CommandType.READ, bucketName, new ReadPayload(key));
            Response response = sendRequest(command);
            if (typeClass.isInstance(response.getValue())) {
                return typeClass.cast(response.getValue());
            } else {
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.trace(e);
            return null;
        }
    }

    public static boolean delete(String bucketName, String key) {
        try {
            Command command = new Command(CommandType.DELETE, bucketName, new DeletePayload(key));
            Response response = sendRequest(command);

            return response.getResponseType() == ResponseType.SUCCESS;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.trace(e);
            return false;
        }
    }
}
