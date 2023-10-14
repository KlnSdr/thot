package thot;

import thot.server.Server;
import thot.buckets.service.BucketService;

public class Main {
    public static void main(String[] args) {
        BucketService.getInstance();

        Server.startApplication();
    }
}
