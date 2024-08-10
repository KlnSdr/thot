package thot;

import dobby.Dobby;
import dobby.DobbyEntryPoint;
import dobby.task.SchedulerService;
import dobby.util.Config;
import dobby.util.logging.Logger;
import thot.buckets.service.BucketService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Thot implements DobbyEntryPoint {
    private static final Logger LOGGER = new Logger(Thot.class);
    private static String basePath = "buckets/";

    public static String getBasePath() {
        return basePath;
    }

    public static void main(String[] args) {
        Dobby.startApplication(Thot.class);
    }

    @Override
    public void preStart() {
        prependJarPathToBasePath();
        thot.buckets.v2.service.BucketService.getInstance();
        createBucketDirectoryIfNeeded();
        migrateOldBuckets();
        thot.buckets.v2.BucketDiscoverer.discoverBuckets("");
    }

    private void migrateOldBuckets() {
        final Set<String> oldBucketNames = BucketService.getInstance().getBucketNames();
        if (oldBucketNames.isEmpty()) {
            return;
        }
        LOGGER.info("Migrating old buckets...");
        LOGGER.info("Creating backup directory...");
        createBackupDir();
        LOGGER.info("Backing up old buckets...");
        final boolean wasBackupSuccessful = copyOldFilesToBackup(oldBucketNames);

        if (!wasBackupSuccessful) {
            LOGGER.error("Failed to backup old buckets. Aborting migration.");
            return;
        }

        final boolean convertWasSuccessful = convertToNewBuckets(oldBucketNames);

        if (!convertWasSuccessful) {
            LOGGER.error("Failed to convert old buckets. Aborting migration.");
            return;
        }

        LOGGER.info("Old buckets migrated successfully!");
        LOGGER.info("Removing old buckets...");

        final boolean didRemoveOldBuckets = removeOldBuckets(oldBucketNames);

        if (!didRemoveOldBuckets) {
            LOGGER.error("Failed to remove old buckets. Aborting migration.");
            return;
        }

        LOGGER.info("migration done!");
    }

    private boolean convertToNewBuckets(Set<String> oldBucketNames) {
        final BucketService oldService = BucketService.getInstance();
        final thot.buckets.v2.service.BucketService newService = thot.buckets.v2.service.BucketService.getInstance();

        AtomicBoolean success = new AtomicBoolean(true);

        oldBucketNames.forEach(bucketName -> {
            LOGGER.info("migrating bucket: " + bucketName);
            final thot.buckets.v2.Bucket newBucket = newService.create(bucketName);

            final thot.buckets.Bucket oldBucket = oldService.find(bucketName);
            final Set<String> keys = oldBucket.getKeys();
            keys.forEach(key -> {
                newBucket.write(key, oldBucket.read(key));
            });

            // ensure bucket is created and synced to disk even if the old one is empty
            if (keys.isEmpty()) {
                newBucket.write("THOT_MIGRATION_TMP_KEY", "TEST");
                newBucket.delete("THOT_MIGRATION_TMP_KEY");
            }

            oldService.delete(bucketName);
        });

        return success.get();
    }

    private boolean removeOldBuckets(Set<String> oldBucketNames) {
        AtomicBoolean success = new AtomicBoolean(true);
        oldBucketNames.forEach(bucketName -> {
            final File bucketFile = new File(basePath + bucketName + ".bucket");
            final boolean didDelete = bucketFile.delete();
            if (!didDelete) {
                LOGGER.error("Failed to delete old bucket file: " + bucketName);
                success.set(false);
            }
        });

        return success.get();
    }

    private boolean copyOldFilesToBackup(Set<String> oldBucketNames) {
        AtomicBoolean success = new AtomicBoolean(true);
        oldBucketNames.forEach(bucketName -> {
            final File bucketFile = new File(basePath + bucketName + ".bucket");
            final File backupFile = new File(basePath + "backup/" + bucketName + ".bucket");
            try {
                Files.copy(bucketFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("Failed to copy bucket file: " + bucketName);
                LOGGER.trace(e);
                success.set(false);
            }
        });
        return success.get();
    }

    private void prependJarPathToBasePath() {
        try {
            final String pathToJar = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            final String jarDir = pathToJar.substring(0, pathToJar.lastIndexOf("/") + 1);
            basePath = jarDir + basePath;
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to get jar path");
            LOGGER.trace(e);
            System.exit(1);
        }
    }

    private void createBucketDirectoryIfNeeded() {
        createDirectoryIfNotExists(basePath);
    }

    private void createBackupDir() {
        createDirectoryIfNotExists(basePath + "backup/");
    }

    private void createDirectoryIfNotExists(String path) {
        final File file = new java.io.File(path);
        if (!file.exists()) {
            final boolean didCreate = file.mkdir();
            if (!didCreate) {
                LOGGER.error("Failed to create bucket directory");
                System.exit(1);
            } else {
                LOGGER.info("Created directory " + path);
            }
        }
    }

    @Override
    public void postStart() {
        Config.getInstance().setBoolean("dobby.scheduler.disabled", false);
        LOGGER.info("adding task to evict buckets every 10 minutes...");
        SchedulerService.getInstance().addRepeating(() -> thot.buckets.v2.service.BucketService.getInstance().evictBuckets(), 10, TimeUnit.MINUTES);
    }
}
