package thot.buckets.v2;

import dobby.util.Classloader;
import dobby.util.logging.Logger;
import thot.buckets.v2.service.BucketService;

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
        if (clazz.isAnnotationPresent(thot.annotations.v2.Bucket.class)) {
            final String bucketName = clazz.getAnnotation(thot.annotations.v2.Bucket.class).value();
            final int maxKeys = clazz.getAnnotation(thot.annotations.v2.Bucket.class).maxKeys();

            Bucket bucket = BucketService.getInstance().find(bucketName);

            if (bucket == null) {
                LOGGER.info("Could not find bucket '" + bucketName + "'. Creating it now");
                bucket = BucketService.getInstance().create(bucketName, maxKeys, 1);

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
