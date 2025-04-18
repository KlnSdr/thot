package thot.buckets;

import dobby.util.Classloader;
import common.logger.Logger;
import thot.buckets.service.BucketService;

public class BucketDiscoverer extends Classloader<Object> {
    private static final Logger LOGGER = new Logger(BucketDiscoverer.class);

    private BucketDiscoverer(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Discovers routes in a given package
     *
     * @param rootPackage Root package
     */
    public static void discoverRoutes(String rootPackage) {
        if (rootPackage.startsWith(".")) {
            rootPackage = rootPackage.substring(1);
        }
        BucketDiscoverer discoverer = new BucketDiscoverer(rootPackage);
        discoverer.loadClasses().forEach(discoverer::analyzeClass);
        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> BucketDiscoverer.discoverRoutes(finalRootPackage + "." + subpackage));
    }

    private void analyzeClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(thot.api.annotations.Bucket.class)) {
            final String bucketName = clazz.getAnnotation(thot.api.annotations.Bucket.class).value();

            Bucket bucket = BucketService.getInstance().find(bucketName);

            if (bucket == null) {
                LOGGER.info("Could not find bucket '" + bucketName + "'. Creating it now");
                bucket = BucketService.getInstance().create(bucketName);

                bucket.write("test", "test_value");
                bucket.delete("test");
            }
        }
    }

    @Override
    protected Class<?> filterClasses(String s) {
        return defaultClassFilter(s);
    }
}
