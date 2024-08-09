package thot.buckets;

import dobby.util.logging.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NewBucket {
    private static final Logger LOGGER = new Logger(NewBucket.class);
    private final String name;
    private final int maxKeys;
    private final ConcurrentHashMap<String, Serializable> data;
    private final ConcurrentHashMap<String, String> subBuckets;
    private boolean isLeaf = true;

    public NewBucket(String name, int maxKeys) {
        this.data = new ConcurrentHashMap<>();
        this.subBuckets = new ConcurrentHashMap<>();
        this.name = name;
        this.maxKeys = maxKeys;
        loadFromDisk();
    }

    public NewBucket(String name) {
        this(name, 1000);
    }

    public void write(String key, Serializable value) {
        LOGGER.debug("Writing to bucket '" + this.name + "' with key '" + key + "'");
        if (this.isLeaf) {
            writeLeaf(key, value);
        } else {
            writeToSubBucket(key, value);
        }
    }

    public Serializable read(String key) {
        LOGGER.debug("Reading from bucket '" + this.name + "' with key '" + key + "'");

        if (this.isLeaf) {
            return this.data.get(key);
        } else {
            return readFromSubBucket(key);
        }
    }

    public Serializable[] readPattern(String pattern) {
        LOGGER.debug("Reading from bucket '" + this.name + "' with pattern '" + pattern + "'");
        if (this.isLeaf) {
            return this.data.entrySet().stream().filter(entry -> entry.getKey().matches(pattern)).map(Map.Entry::getValue).toArray(Serializable[]::new);
        } else {
            return readPatternFromSubBucket(pattern);
        }
    }

    public void delete(String key) {
        LOGGER.debug("Deleting from bucket '" + this.name + "' with key '" + key + "'");
        if (this.isLeaf) {
            Serializable oldValue = this.data.remove(key);
            if (oldValue != null) {
                saveToDisk();
            }
        } else {
            deleteFromSubBucket(key);
        }
    }

    public String[] getKeys() {
        if (this.isLeaf) {
            return this.data.keySet().toArray(new String[0]);
        } else {
            return getKeysFromSubBucket();
        }
    }

    private String[] getKeysFromSubBucket() {
        LOGGER.warn("getKeysFromSubBucket not implemented");
        return new String[0];
    }

    private void deleteFromSubBucket(String key) {
        LOGGER.warn("deleteFromSubBucket not implemented");
    }

    private Serializable[] readPatternFromSubBucket(String pattern) {
        LOGGER.warn("readPatternFromSubBucket not implemented");
        return new Serializable[0];
    }

    private Serializable readFromSubBucket(String key) {
        LOGGER.warn("readFromSubBucket not implemented");
        return null;
    }

    private void writeLeaf(String key, Serializable value) {
        this.data.put(key, value);
        saveToDisk();

        if (this.data.size() > this.maxKeys) {
            splitBucket();
        }
    }

    private void splitBucket() {
        LOGGER.warn("splitBucket not implemented");
    }

    private void writeToSubBucket(String key, Serializable value) {
        LOGGER.warn("writeToSubBucket not implemented");
    }

    private void loadFromDisk() {
        LOGGER.warn("loadFromDisk not implemented");
    }

    private void saveToDisk() {
        LOGGER.warn("saveToDisk not implemented");
    }
}
