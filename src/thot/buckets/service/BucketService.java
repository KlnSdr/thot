package thot.buckets.service;

import thot.buckets.Bucket;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static thot.Thot.getBasePath;

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

    public Set<String> getBucketNames() {
        return this.buckets.keySet();
    }

    public Bucket find(String name) {
        return this.buckets.get(name);
    }

    public Bucket create(String name) {
        if (this.buckets.containsKey(name)) {
            throw new IllegalArgumentException("Bucket already exists");
        }
        this.buckets.put(name, new Bucket(name));
        return find(name);
    }

    public void delete(String name) {
        if (!this.buckets.containsKey(name)) {
            throw new IllegalArgumentException("Bucket does not exist");
        }
        this.buckets.remove(name);
    }

    private void loadBucketsFromDisk() {
        String[] buckets = getBuckets();
        for (String bucketName : buckets) {
            this.buckets.put(bucketName, new Bucket(bucketName));
        }
    }

    private String[] getBuckets() {
        File folder = new File(getBasePath());
        ArrayList<String> buckets = new ArrayList<>();

        if (!folder.exists() || !folder.isDirectory()) {
            return new String[0];
        }

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".bucket")) {
                buckets.add(file.getName().replace(".bucket", ""));
            }
        }
        return buckets.toArray(new String[0]);
    }
}
