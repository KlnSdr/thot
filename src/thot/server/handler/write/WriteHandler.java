package thot.server.handler.write;

import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;
import thot.buckets.Bucket;
import thot.buckets.service.BucketService;

public class WriteHandler implements Handler {
    @Override
    public Response handle(Command command) {
        String tableName = command.getTable();
        WritePayload payload = (WritePayload) command.getPayload();

        BucketService bucketService = BucketService.getInstance();

        Bucket bucket = bucketService.find(tableName);
        if (bucket == null) {
            bucket = bucketService.create(tableName);
        }

        bucket.write(payload.getKey(), payload.getValue());

        Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        return response;
    }
}
