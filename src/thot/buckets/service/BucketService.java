package thot.buckets.service;

import thot.buckets.Bucket;

import java.util.concurrent.ConcurrentHashMap;

public class BucketService {
    private static BucketService instance;
    private final ConcurrentHashMap<String, Bucket> tables;

    private BucketService() {
        this.tables = new ConcurrentHashMap<>();
        loadTablesFromDisk();
    }

    public static BucketService getInstance() {
        if (instance == null) {
            instance = new BucketService();
        }
        return instance;
    }

    public Bucket find(String name) {
        return this.tables.get(name);
    }

    public Bucket create(String name) {
        if (this.tables.containsKey(name)) {
            throw new IllegalArgumentException("Table already exists");
        }
        this.tables.put(name, new Bucket());
        return find(name);
    }

    public void delete(String name) {
        if (!this.tables.containsKey(name)) {
            throw new IllegalArgumentException("Table does not exist");
        }
        this.tables.remove(name);
    }

    private void loadTablesFromDisk() {
        // todo implement
    }
}
