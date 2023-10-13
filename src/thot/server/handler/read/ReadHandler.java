package thot.server.handler.read;

import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;
import thot.tables.Table;
import thot.tables.service.TableService;

import java.io.Serializable;

public class ReadHandler implements Handler {
    @Override
    public Response handle(Command command) {
        String tableName = command.getTable();
        ReadPayload payload = (ReadPayload) command.getPayload();

        TableService tableService = TableService.getInstance();

        Table table = tableService.find(tableName);
        if (table == null) {
            return tableNotFoundResponse(tableName);
        }

        Serializable value = table.read(payload.getKey());
        if (value == null) {
            return keyNotFoundResponse(payload.getKey(), tableName);
        }

        return successResponse(value);
    }

    private Response tableNotFoundResponse(String tableName) {
        Response response = new Response();
        response.setResponseType(ResponseType.ERROR);
        response.setError("Table '" + tableName + "' not found");
        return response;
    }

    private Response keyNotFoundResponse(String key, String tableName) {
        Response response = new Response();
        response.setResponseType(ResponseType.ERROR);
        response.setError("Key '" + key + "' in table '" + tableName + "' not found");
        return response;
    }

    private Response successResponse(Serializable value) {
        Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        response.setValue(value);
        return response;
    }
}
