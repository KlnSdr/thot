package thot;

import dobby.Dobby;
import dobby.DobbyEntryPoint;
import dobby.util.logging.Logger;
import thot.buckets.service.BucketService;

import java.net.URISyntaxException;

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
        BucketService.getInstance();
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

    @Override
    public void postStart() {
    }
}
