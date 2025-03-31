package thot.server.handler.create;

import thot.api.command.Command;
import thot.api.payload.CreatePayload;
import thot.api.response.Response;
import thot.api.response.ResponseType;
import thot.buckets.v2.Bucket;
import thot.buckets.v2.service.BucketService;
import thot.server.handler.Handler;

public class CreateHandler implements Handler {
    @Override
    public Response handle(Command command) {
        final Bucket bucket;
        try {

        CreatePayload payload = (CreatePayload) command.getPayload();

        BucketService bucketService = BucketService.getInstance();

        bucket = bucketService.create(payload.getName(), payload.getMaxKeys(), 1, payload.isVolatile());
        } catch (Exception e) {
            return errorResponse(e.getMessage());
        }

        if (bucket == null) {
            return errorResponse("An unknown error occurred while creating the bucket");
        }

        return successResponse();
    }

    private Response errorResponse(String errorMessage) {
        Response response = new Response();
        response.setResponseType(ResponseType.ERROR);
        response.setError(errorMessage);
        return response;
    }

    private Response successResponse() {
        Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        return response;
    }
}
