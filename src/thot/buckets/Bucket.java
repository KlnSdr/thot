package thot.buckets;

import thot.util.logging.Logger;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import static thot.Main.basePath;

public class Bucket implements Serializable {
    private static final Logger LOGGER = new Logger(Bucket.class);
    private final String name;
    private final ConcurrentHashMap<String, Serializable> bucket;

    public Bucket(String name) {
        this.bucket = new ConcurrentHashMap<>();
        this.name = name;
        loadFromDisk();
    }

    public void write(String key, Serializable value) {
        LOGGER.info("Writing to bucket '" + this.name + "' with key '" + key + "'");
        this.bucket.put(key, value);
        saveToDisk();
    }

    public Serializable read(String key) {
        LOGGER.info("Reading from bucket '" + this.name + "' with key '" + key + "'");
        return this.bucket.get(key);
    }

    public void delete(String key) {
        LOGGER.info("Deleting from bucket '" + this.name + "' with key '" + key + "'");
        Serializable oldValue = this.bucket.remove(key);
        if (oldValue != null) {
            saveToDisk();
        }
    }

    public boolean contains(String key) {
        return this.bucket.containsKey(key);
    }

    private void saveToDisk() {
        LOGGER.info("Saving bucket '" + this.name + "' to disk");
        try {
            FileOutputStream fos = new FileOutputStream(basePath + this.name + ".bucket");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.bucket);
            oos.close();
            fos.close();
        } catch (IOException e) {
            LOGGER.error("Failed to save bucket '" + this.name + "' to disk");
            LOGGER.trace(e);
        }
    }

    private void loadFromDisk() {
        LOGGER.info("Loading bucket '" + this.name + "' from disk");
        File file = new File(basePath + this.name + ".bucket");
        if (!file.exists() || !file.isFile()) {
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object object = ois.readObject();

            if (object instanceof ConcurrentHashMap) {
                this.bucket.putAll((ConcurrentHashMap<String, Serializable>) object);
            } else {
                LOGGER.error("Failed to load bucket '" + this.name + "' from disk");
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Failed to load bucket '" + this.name + "' from disk");
            LOGGER.trace(e);
        }
    }
}
