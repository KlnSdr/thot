package thot;

import thot.buckets.service.BucketService;
import thot.server.Server;

public class Main {
    public static final String basePath = "buckets/";

    public static void main(String[] args) {
        Server.startApplication();
    }
}
