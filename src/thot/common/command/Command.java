package thot.common.command;

import java.io.Serializable;

public class Command implements Serializable {
    private final CommandType commandType;
    private final String table;
    private final Serializable payload;

    public Command(CommandType commandType, String table, Serializable payload) {
        this.commandType = commandType;
        this.table = table;
        this.payload = payload;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getTable() {
        return table;
    }

    public Object getPayload() {
        return payload;
    }
}
