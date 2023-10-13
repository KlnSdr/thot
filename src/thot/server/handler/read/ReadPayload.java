package thot.server.handler.read;

import java.io.Serializable;

public class ReadPayload implements Serializable {
    private final String key;

    public ReadPayload(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
