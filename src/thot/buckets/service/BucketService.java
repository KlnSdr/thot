package thot.buckets.service;

import thot.buckets.Bucket;

import java.util.concurrent.ConcurrentHashMap;

public class BucketService {
    private static BucketService instance;
    private final ConcurrentHashMap<String, Bucket> buckets;

    private BucketService() {
        this.buckets = new ConcurrentHashMap<>();
        loadBucketsFromDisk();
    }

    public static BucketService getInstance() {
        if (instance == null) {
            instance = new BucketService();
        }
        return instance;
    }

    public Bucket find(String name) {
        return this.buckets.get(name);
    }

    public Bucket create(String name) {
        if (this.buckets.containsKey(name)) {
            throw new IllegalArgumentException("Bucket already exists");
        }
        this.buckets.put(name, new Bucket());
        return find(name);
    }

    public void delete(String name) {
        if (!this.buckets.containsKey(name)) {
            throw new IllegalArgumentException("Bucket does not exist");
        }
        this.buckets.remove(name);
    }

    private void loadBucketsFromDisk() {
        // todo implement
    }
}
