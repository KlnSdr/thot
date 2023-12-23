package thot;

import dobby.Dobby;
import dobby.DobbyEntryPoint;
import dobby.task.SchedulerService;
import thot.buckets.service.BucketService;

public class Main implements DobbyEntryPoint {
    public static final String basePath = "buckets/";

    public static void main(String[] args) {
        Dobby.startApplication(Main.class);
    }

    @Override
    public void preStart() {
        BucketService.getInstance();
    }

    @Override
    public void postStart() {
        SchedulerService.getInstance().stopAll();
    }
}
