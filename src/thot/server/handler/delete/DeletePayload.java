package thot.server.handler.delete;

import java.io.Serializable;

public class DeletePayload implements Serializable {
    private final String key;

    public DeletePayload(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
