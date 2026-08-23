package thot.buckets.v2.service;

import common.logger.Logger;
import thot.buckets.v2.Bucket;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static thot.Thot.getBasePath;

public class BucketService {
    private static final Logger LOGGER = new Logger(BucketService.class);
    private static volatile BucketService instance;
    private final ConcurrentHashMap<String, Date> lastAccessed;
    private final ConcurrentHashMap<String, Bucket> buckets;
    private final Set<String> knownBuckets;
    private final Set<String> volatileBuckets;

    private BucketService() {
        this.buckets = new ConcurrentHashMap<>();
        this.lastAccessed = new ConcurrentHashMap<>();
        this.knownBuckets = ConcurrentHashMap.newKeySet();
        this.volatileBuckets = ConcurrentHashMap.newKeySet();
        loadBucketsFromDisk();

        Runtime.getRuntime().addShutdownHook(new Thread(this::flushDirtyBuckets, "thot-bucket-flush-on-shutdown"));
    }

    public static BucketService getInstance() {
        BucketService result = instance;
        if (result == null) {
            synchronized (BucketService.class) {
                result = instance;
                if (result == null) {
                    instance = result = new BucketService();
                }
            }
        }
        return result;
    }

    public Set<String> getBucketNames() {
        return new HashSet<>(this.knownBuckets);
    }

    public Bucket find(String name) {
        if (!this.knownBuckets.contains(name)) {
            return null;
        }

        final Bucket bucket = this.buckets.computeIfAbsent(name, Bucket::new);

        updateLastAccessed(name);

        return bucket;
    }

    public String[] getKeys(String name) {
        final Bucket bucket = find(name);
        if (bucket == null) {
            return new String[0];
        }
        return bucket.getKeys();
    }

    public synchronized Bucket create(String name, int maxKeys, int hashLength, boolean isVolatile) {
        if (this.knownBuckets.contains(name)) {
            throw new IllegalArgumentException("Bucket already exists");
        }
        this.buckets.put(name, new Bucket(name, maxKeys, hashLength, isVolatile));
        this.knownBuckets.add(name);
        if (isVolatile) {
            this.volatileBuckets.add(name);
        }
        return find(name);
    }

    public synchronized Bucket create(String name, int maxKeys, int hashLength) {
        if (this.knownBuckets.contains(name)) {
            throw new IllegalArgumentException("Bucket already exists");
        }
        this.buckets.put(name, new Bucket(name, maxKeys, hashLength));
        this.knownBuckets.add(name);
        return find(name);
    }

    public synchronized Bucket create(String name) {
        if (this.knownBuckets.contains(name)) {
            throw new IllegalArgumentException("Bucket already exists");
        }
        this.buckets.put(name, new Bucket(name));
        this.knownBuckets.add(name);
        return find(name);
    }

    public synchronized Bucket getOrCreate(String name, int maxKeys, int hashLength, boolean isVolatile) {
        if (this.knownBuckets.contains(name)) {
            return find(name);
        }
        return create(name, maxKeys, hashLength, isVolatile);
    }

    public synchronized Bucket getOrCreate(String name) {
        return getOrCreate(name, 200, 1, false);
    }

    public synchronized void delete(String name) {
        if (!this.knownBuckets.contains(name)) {
            throw new IllegalArgumentException("Bucket does not exist");
        }
        this.buckets.remove(name);
        this.lastAccessed.remove(name);
        this.knownBuckets.remove(name);
        this.volatileBuckets.remove(name);
    }

    public synchronized void evictBuckets() {
        final Date now = new Date();
        for (String name : this.lastAccessed.keySet()) {
            final Date lastAccessed = this.lastAccessed.get(name);
            if (lastAccessed != null && now.getTime() - lastAccessed.getTime() > 3_600_000 /* 1 h */ && !this.volatileBuckets.contains(name)) {
                final Bucket bucket = this.buckets.get(name);
                if (bucket != null) {
                    bucket.flushIfDirty();
                }
                this.buckets.remove(name);
                this.lastAccessed.remove(name);
                LOGGER.debug("Evicted bucket '" + name + "'");
            }
        }
    }

    public void flushDirtyBuckets() {
        for (Bucket bucket : this.buckets.values()) {
            bucket.flushIfDirty();
        }
    }

    private void loadBucketsFromDisk() {
        final String[] buckets = getBuckets();
        this.knownBuckets.addAll(Arrays.asList(buckets)); // only load names, not the actual buckets -> lazy loading
    }

    private String[] getBuckets() {
        File folder = new File(getBasePath());
        ArrayList<String> bucketNames = new ArrayList<>();

        if (!folder.exists() || !folder.isDirectory()) {
            return new String[0];
        }

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".config")) {
                bucketNames.add(file.getName().replace(".config", ""));
            }
        }
        return bucketNames.toArray(new String[0]);
    }

    private void updateLastAccessed(String name) {
        this.lastAccessed.put(name, new Date());
    }
}
