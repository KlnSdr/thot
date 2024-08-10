package thot.server.handler.names;

import thot.buckets.v2.service.BucketService;
import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;

public class BucketNameHandler implements Handler {
    @Override
    public Response handle(Command command) {
        final BucketService bucketService = BucketService.getInstance();

        final Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        final String[] bucketNamesArray = bucketService.getBucketNames().stream().filter(bucketName -> !bucketName.contains("-")).distinct().sorted().toArray(String[]::new);
        response.setValue(bucketNamesArray);

        return response;
    }
}
