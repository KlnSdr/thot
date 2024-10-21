package thot.server.handler.write;

import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;
import thot.buckets.v2.Bucket;
import thot.buckets.v2.service.BucketService;

public class WriteHandler implements Handler {
    @Override
    public Response handle(Command command) {
        String bucketName = command.getBucketName();
        WritePayload payload = (WritePayload) command.getPayload();

        BucketService bucketService = BucketService.getInstance();

        Bucket bucket = bucketService.find(bucketName);
        if (bucket == null) {
            if (payload.getCreateVolatile()) {
                bucket = bucketService.create(bucketName, 100, 1, true);
            } else {
                bucket = bucketService.create(bucketName);
            }
        }

        bucket.write(payload.getKey(), payload.getValue());

        Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        return response;
    }
}
