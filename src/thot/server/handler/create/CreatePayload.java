package thot.server.handler.create;

import java.io.Serializable;

public class CreatePayload implements Serializable {
    private final String name;
    private final int maxKeys;
    private final boolean isVolatile;

    public CreatePayload(String name, int maxKeys, boolean isVolatile) {
        this.name = name;
        this.maxKeys = maxKeys;
        this.isVolatile = isVolatile;
    }

    public String getName() {
        return name;
    }

    public int getMaxKeys() {
        return maxKeys;
    }

    public boolean isVolatile() {
        return isVolatile;
    }
}
