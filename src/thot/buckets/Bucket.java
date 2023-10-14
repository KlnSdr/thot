package thot.buckets;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Bucket {
    private final ConcurrentHashMap<String, Serializable> bucket;

    public Bucket() {
        this.bucket = new ConcurrentHashMap<>();
    }

    public void write(String key, Serializable value) {
        this.bucket.put(key, value);
    }

    public Serializable read(String key) {
        return this.bucket.get(key);
    }

    public void delete(String key) {
        this.bucket.remove(key);
    }

    public boolean contains(String key) {
        return this.bucket.containsKey(key);
    }

    private void saveToDisk() {
        // todo implement
    }
}
