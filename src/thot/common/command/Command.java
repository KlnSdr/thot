package thot.common.command;

import java.io.Serializable;

public class Command implements Serializable {
    private final CommandType commandType;
    private final String bucketName;
    private final Serializable payload;

    public Command(CommandType commandType, String bucketName, Serializable payload) {
        this.commandType = commandType;
        this.bucketName = bucketName;
        this.payload = payload;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getBucketName() {
        return bucketName;
    }

    public Object getPayload() {
        return payload;
    }
}
