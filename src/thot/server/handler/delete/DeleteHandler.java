package thot.server.handler.delete;

import thot.buckets.v2.Bucket;
import thot.buckets.v2.service.BucketService;
import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;

import java.io.Serializable;

public class DeleteHandler implements Handler {
    @Override
    public Response handle(Command command) {
        String bucketName = command.getBucketName();
        DeletePayload payload = (DeletePayload) command.getPayload();

        BucketService bucketService = BucketService.getInstance();

        Bucket bucket = bucketService.find(bucketName);
        if (bucket == null) {
            return bucketNotFoundResponse(bucketName);
        }

        Serializable value = bucket.read(payload.getKey());
        if (value == null) {
            return keyNotFoundResponse(payload.getKey(), bucketName);
        }
        bucket.delete(payload.getKey());

        return successResponse();
    }

    private Response bucketNotFoundResponse(String bucketName) {
        Response response = new Response();
        response.setResponseType(ResponseType.ERROR);
        response.setError("Bucket '" + bucketName + "' not found");
        return response;
    }

    private Response keyNotFoundResponse(String key, String bucketName) {
        Response response = new Response();
        response.setResponseType(ResponseType.ERROR);
        response.setError("Key '" + key + "' in bucket '" + bucketName + "' not found");
        return response;
    }

    private Response successResponse() {
        Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        return response;
    }
}
