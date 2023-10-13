package thot.server.handler.write;

import java.io.Serializable;

public class WritePayload implements Serializable {
    private final String key;
    private final Serializable value;

    public WritePayload(String key, Serializable value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Serializable getValue() {
        return value;
    }
}
