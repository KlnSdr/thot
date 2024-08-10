package thot.server.handler.keys;

import thot.buckets.v2.Bucket;
import thot.buckets.v2.service.BucketService;
import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;

import java.io.Serializable;

public class KeysHandler implements Handler {
    @Override
    public Response handle(Command command) {
        String bucketName = command.getBucketName();

        BucketService bucketService = BucketService.getInstance();

        Bucket bucket = bucketService.find(bucketName);
        if (bucket == null) {
            return bucketNotFoundResponse(bucketName);
        }

        return successResponse(bucket.getKeys());
    }

    private Response successResponse(Serializable[] value) {
        Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        response.setValue(value);
        return response;
    }

    private Response bucketNotFoundResponse(String bucketName) {
        Response response = new Response();
        response.setResponseType(ResponseType.ERROR);
        response.setError("Bucket '" + bucketName + "' not found");
        return response;
    }

    private Response errorWhileAccessingBucket(String bucketName) {
        Response response = new Response();
        response.setResponseType(ResponseType.ERROR);
        response.setError("Error while accessing bucket '" + bucketName + "'");
        return response;
    }
}
