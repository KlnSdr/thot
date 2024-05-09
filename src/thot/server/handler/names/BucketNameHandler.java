package thot.server.handler.names;

import thot.buckets.service.BucketService;
import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;

import java.io.Serializable;
import java.util.Set;

public class BucketNameHandler implements Handler {
    @Override
    public Response handle(Command command) {
        final BucketService bucketService = BucketService.getInstance();

        final Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        final Set<String> bucketNames = bucketService.getBucketNames();
        response.setValue(bucketNames.toArray(new String[0]));

        return response;
    }
}
