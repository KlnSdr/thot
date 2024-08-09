package thot.connector;

import dobby.util.logging.Logger;
import thot.common.command.Command;
import thot.common.command.CommandType;
import thot.common.command.KeyType;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.delete.DeletePayload;
import thot.server.handler.read.ReadPayload;
import thot.server.handler.write.WritePayload;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.Arrays;

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
        boolean didWrite2 = write("temperatureValues", "2023-01-02", "23.7");
        LOGGER.info("write: " + didWrite2);
        String[] val3 = readPattern("temperatureValues", "2023-01-0.*", String.class);
        LOGGER.info("read pattern: " + Arrays.toString(val3));
        String[] keys = getKeys("temperatureValues");
        LOGGER.info("keys: " + Arrays.toString(keys));

        String[] buckets = getBuckets();
        LOGGER.info("buckets: " + Arrays.toString(buckets));
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

    @SuppressWarnings("unchecked")
    public static <T> T[] readPattern(String bucketName, String pattern, Class<? extends T> comonentTypeClass) {
        try {
            Command command = new Command(CommandType.READ, bucketName, new ReadPayload(pattern, KeyType.REGEX));
            Response response = sendRequest(command);
            Object[] responseArray = (Object[]) response.getValue();
            if (responseArray.length == 0) {
                return (T[]) Array.newInstance(comonentTypeClass, 0);
            } else if (comonentTypeClass.isInstance(responseArray[0])) {
                T[] arr = (T[]) Array.newInstance(comonentTypeClass, responseArray.length);
                for (int i = 0; i < responseArray.length; i++) {
                    arr[i] = comonentTypeClass.cast(responseArray[i]);
                }
                return arr;
            } else {
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.trace(e);
            return null;
        }
    }

    public static <T> T read(String bucketName, String key, Class<? extends T> typeClass) {
        try {
            Command command = new Command(CommandType.READ, bucketName, new ReadPayload(key, KeyType.ABSOLUTE));
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

    public static String[] getKeys(String bucketName) {
        try {
            Command command = new Command(CommandType.KEYS, bucketName, null);
            Response response = sendRequest(command);
            if (response.getResponseType() == ResponseType.SUCCESS) {
                return (String[]) response.getValue();
            } else {
                return new String[0];
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.trace(e);
            return new String[0];
        }
    }

    public static String[] getBuckets() {
        try {
            Command command = new Command(CommandType.BUCKETS, null, null);
            Response response = sendRequest(command);
            if (response.getResponseType() == ResponseType.SUCCESS) {
                return (String[]) response.getValue();
            } else {
                return new String[0];
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.trace(e);
            return new String[0];
        }
    }

    public static boolean deletePattern(String bucketName, String pattern) {
        return delete(bucketName, pattern, KeyType.REGEX);
    }

    public static boolean delete(String bucketName, String key) {
        return delete(bucketName, key, KeyType.ABSOLUTE);
    }

    private static boolean delete(String bucketName, String key, KeyType keyType) {
        try {
            Command command = new Command(CommandType.DELETE, bucketName, new DeletePayload(key, keyType));
            Response response = sendRequest(command);

            return response.getResponseType() == ResponseType.SUCCESS;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.trace(e);
            return false;
        }
    }
}
