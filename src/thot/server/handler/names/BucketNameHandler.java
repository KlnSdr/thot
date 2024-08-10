package thot.server.handler.names;

import thot.buckets.v2.service.BucketService;
import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;

import java.util.Arrays;
import java.util.Set;

public class BucketNameHandler implements Handler {
    @Override
    public Response handle(Command command) {
        final BucketService bucketService = BucketService.getInstance();

        final Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        final Set<String> bucketNames = bucketService.getBucketNames();
        bucketNames.stream().filter(bucketName -> bucketName.contains("-")).forEach(bucketNames::remove); // filter out all sub-buckets
        final String[] bucketNamesArray = bucketNames.toArray(new String[0]);
        Arrays.sort(bucketNamesArray);
        response.setValue(bucketNamesArray);

        return response;
    }
}
