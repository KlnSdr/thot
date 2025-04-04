package thot.buckets.service;

import common.logger.Logger;
import thot.buckets.Bucket;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static thot.Thot.getBasePath;

public class BucketService {
    private static final Logger LOGGER = new Logger(BucketService.class);
    private static BucketService instance;
    private final ConcurrentHashMap<String, Date> lastAccessed;
    private final ConcurrentHashMap<String, Bucket> buckets;
    private final List<String> knownBuckets;

    private BucketService() {
        this.buckets = new ConcurrentHashMap<>();
        this.lastAccessed = new ConcurrentHashMap<>();
        this.knownBuckets = new ArrayList<>();
        loadBucketsFromDisk();
    }

    public static BucketService getInstance() {
        if (instance == null) {
            instance = new BucketService();
        }
        return instance;
    }

    public Set<String> getBucketNames() {
        return this.knownBuckets.stream().collect(HashSet::new, HashSet::add, HashSet::addAll); // just to not break existing code
    }

    public Bucket find(String name) {
        if (!this.knownBuckets.contains(name)) {
            return null;
        }

        Bucket bucket;
        if (!this.buckets.containsKey(name)) {
            bucket = new Bucket(name);
            this.buckets.put(name, bucket);
        } else {
            bucket = this.buckets.get(name);
        }

        updateLastAccessed(name);

        return bucket;
    }

    public Bucket create(String name) {
        if (this.knownBuckets.contains(name)) {
            throw new IllegalArgumentException("Bucket already exists");
        }
        this.buckets.put(name, new Bucket(name));
        this.knownBuckets.add(name);
        return find(name);
    }

    public void delete(String name) {
        if (!this.knownBuckets.contains(name)) {
            throw new IllegalArgumentException("Bucket does not exist");
        }
        this.buckets.remove(name);
        this.lastAccessed.remove(name);
        this.knownBuckets.remove(name);
    }

    public void evictBuckets() {
        final Date now = new Date();
        for (String name : this.lastAccessed.keySet()) {
            final Date lastAccessed = this.lastAccessed.get(name);
            if (now.getTime() - lastAccessed.getTime() > 3_600_000 /* 1 h */) {
                this.buckets.remove(name);
                this.lastAccessed.remove(name);
                LOGGER.debug("Evicted bucket '" + name + "'");
            }
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
            if (file.isFile() && file.getName().endsWith(".bucket")) {
                bucketNames.add(file.getName().replace(".bucket", ""));
            }
        }
        return bucketNames.toArray(new String[0]);
    }

    private void updateLastAccessed(String name) {
        this.lastAccessed.put(name, new Date());
    }
}
