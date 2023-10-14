package thot.server.handler.read;

import thot.common.command.KeyType;

import java.io.Serializable;

public class ReadPayload implements Serializable {
    private final String key;
    private final KeyType keyType;

    public ReadPayload(String key, KeyType keyType) {
        this.key = key;
        this.keyType = keyType;
    }

    public String getKey() {
        return key;
    }

    public KeyType getKeyType() {
        return keyType;
    }
}
