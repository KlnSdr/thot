package thot.server.handler.read;

import thot.api.command.Command;
import thot.api.command.KeyType;
import thot.api.payload.ReadPayload;
import thot.api.response.Response;
import thot.api.response.ResponseType;
import thot.buckets.v2.Bucket;
import thot.buckets.v2.service.BucketService;
import thot.server.handler.Handler;

import java.io.Serializable;

public class ReadHandler implements Handler {
    @Override
    public Response handle(Command command) {
        String bucketName = command.getBucketName();
        ReadPayload payload = (ReadPayload) command.getPayload();
        KeyType keyType = payload.getKeyType();

        BucketService bucketService = BucketService.getInstance();

        Bucket bucket = bucketService.find(bucketName);
        if (bucket == null) {
            return bucketNotFoundResponse(bucketName);
        }

        if (keyType == KeyType.ABSOLUTE) {
            Serializable value = bucket.read(payload.getKey());
            if (value == null) {
                return keyNotFoundResponse(payload.getKey(), bucketName);
            }

            return successResponse(value);
        } else if (keyType == KeyType.REGEX) {
            Serializable[] values = bucket.readPattern(payload.getKey());
            if (values == null) {
                return keyNotFoundResponse(payload.getKey(), bucketName);
            }

            return successResponse(values);
        } else {
            return keyNotFoundResponse(payload.getKey(), bucketName);
        }
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

    private Response successResponse(Serializable[] value) {
        Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        response.setValue(value);
        return response;
    }
}
