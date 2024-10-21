package thot.server.handler.write;

import java.io.Serializable;

public class WritePayload implements Serializable {
    private final String key;
    private final Serializable value;
    private final boolean createVolatileBucket;

    public WritePayload(String key, Serializable value, boolean createVolatileBucketWhenNotFound) {
        this.key = key;
        this.value = value;
        this.createVolatileBucket = createVolatileBucketWhenNotFound;
    }

    public String getKey() {
        return key;
    }

    public Serializable getValue() {
        return value;
    }

    public boolean getCreateVolatile() {
        return createVolatileBucket;
    }
}
