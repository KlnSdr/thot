package thot.server.handler.read;

import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;
import thot.buckets.Bucket;
import thot.buckets.service.BucketService;

import java.io.Serializable;

public class ReadHandler implements Handler {
    @Override
    public Response handle(Command command) {
        String bucketName = command.getBucketName();
        ReadPayload payload = (ReadPayload) command.getPayload();

        BucketService bucketService = BucketService.getInstance();

        Bucket bucket = bucketService.find(bucketName);
        if (bucket == null) {
            return bucketNotFoundResponse(bucketName);
        }

        Serializable value = bucket.read(payload.getKey());
        if (value == null) {
            return keyNotFoundResponse(payload.getKey(), bucketName);
        }

        return successResponse(value);
    }

    private Response bucketNotFoundResponse(String bucketName) {
        Response response = new Response();
        response.setResponseType(ResponseType.ERROR);
        response.setError("bucket '" + bucketName + "' not found");
        return response;
    }

    private Response keyNotFoundResponse(String key, String bucketName) {
        Response response = new Response();
        response.setResponseType(ResponseType.ERROR);
        response.setError("Key '" + key + "' in bucket '" + bucketName + "' not found");
        return response;
    }

    private Response successResponse(Serializable value) {
        Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        response.setValue(value);
        return response;
    }
}
