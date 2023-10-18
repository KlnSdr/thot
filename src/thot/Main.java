package thot;

import dobby.Dobby;
import thot.buckets.service.BucketService;

public class Main {
    public static final String basePath = "buckets/";

    public static void main(String[] args) {
        BucketService.getInstance();
        Dobby.startApplication(Main.class);
    }
}
