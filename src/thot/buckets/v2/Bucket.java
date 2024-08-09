package thot.buckets.v2;

import dobby.util.logging.Logger;
import thot.buckets.v2.service.BucketService;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Bucket {
    private static final Logger LOGGER = new Logger(Bucket.class);
    private final String name;
    private final int maxKeys;
    private final int keyHashSubstringLength;
    private final ConcurrentHashMap<String, Serializable> data;
    private final ConcurrentHashMap<String, String> subBuckets;
    private boolean isLeaf = true;

    public Bucket(String name, int maxKeys, int keyHashSubstringLength) {
        this.data = new ConcurrentHashMap<>();
        this.subBuckets = new ConcurrentHashMap<>();
        this.name = name;
        this.maxKeys = maxKeys;
        this.keyHashSubstringLength = keyHashSubstringLength;
        loadFromDisk();
    }

    public Bucket(String name, int maxKeys) {
        this(name, maxKeys, 1);
    }

    public Bucket(String name) {
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

    private String getSubBucketFor(String key) {
        if (this.isLeaf) {
            LOGGER.warn("getSubBucketFor called on leaf bucket");
            return null;
        }

        final String keyHash = calculateKeyHash(key);
        if (keyHash == null) {
            return null;
        }

        return getSubBucketForHash(keyHash.substring(0, this.keyHashSubstringLength));
    }

    private String getSubBucketForHash(String keyHash) {
        if (this.isLeaf) {
            LOGGER.warn("getSubBucketForHash called on leaf bucket");
            return null;
        }

        String bucketName = this.subBuckets.get(keyHash);
        if (bucketName == null) {
            LOGGER.info("No sub-bucket found for key hash '" + keyHash + "', creating new sub-bucket");
            bucketName = this.name + "-" + keyHash;
            this.subBuckets.put(keyHash, bucketName);
            BucketService.getInstance().create(bucketName, maxKeys, keyHashSubstringLength + 1);
        }
        return bucketName;
    }

    private String calculateKeyHash(String key) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashBytes = digest.digest(key.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Failed to calculate hash for key '" + key + "'");
            LOGGER.trace(e);
            return null;
        }
    }

    private String[] getKeysFromSubBucket() {
        final ArrayList<String> keys = new ArrayList<>();
        for (String subBucketName : this.subBuckets.values()) {
            final Bucket subBucket = BucketService.getInstance().find(subBucketName);
            if (subBucket != null) {
                Collections.addAll(keys, subBucket.getKeys());
            }
        }
        return keys.toArray(new String[0]);
    }

    private void deleteFromSubBucket(String key) {
        final String subBucketName = getSubBucketFor(key);
        if (subBucketName != null) {
            final Bucket subBucket = BucketService.getInstance().find(subBucketName);
            if (subBucket != null) {
                subBucket.delete(key);
            }
        }
    }

    private Serializable[] readPatternFromSubBucket(String pattern) {
        final ArrayList<Serializable> values = new ArrayList<>();
        for (String subBucketName : this.subBuckets.values()) {
            final Bucket subBucket = BucketService.getInstance().find(subBucketName);
            if (subBucket != null) {
                Collections.addAll(values, subBucket.readPattern(pattern));
            }
        }
        return values.toArray(new Serializable[0]);
    }

    private Serializable readFromSubBucket(String key) {
        final String subBucketName = getSubBucketFor(key);
        if (subBucketName != null) {
            final Bucket subBucket = BucketService.getInstance().find(subBucketName);
            if (subBucket != null) {
                return subBucket.read(key);
            }
        }
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
        final ConcurrentHashMap<String, Serializable> newData = new ConcurrentHashMap<>(this.data);
        this.data.clear();
        this.isLeaf = false;

        for (Map.Entry<String, Serializable> entry : newData.entrySet()) {
            final String subBucketName = getSubBucketFor(entry.getKey());
            writeToSubBucket(subBucketName, entry.getKey(), entry.getValue());
        }
    }

    private void writeToSubBucket(String bucketName, String key, Serializable value) {
        final Bucket subBucket = BucketService.getInstance().find(bucketName);
        if (subBucket != null) {
            subBucket.write(key, value);
        }
    }

    private void writeToSubBucket(String key, Serializable value) {
        final String subBucketName = getSubBucketFor(key);
        writeToSubBucket(subBucketName, key, value);
    }

    private void loadFromDisk() {
        LOGGER.warn("loadFromDisk not implemented");
    }

    private void saveToDisk() {
        LOGGER.warn("saveToDisk not implemented");
    }
}
