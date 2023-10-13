package thot.server.handler.write;

import thot.common.command.Command;
import thot.common.response.Response;
import thot.common.response.ResponseType;
import thot.server.handler.Handler;
import thot.tables.Table;
import thot.tables.service.TableService;

public class WriteHandler implements Handler {
    @Override
    public Response handle(Command command) {
        String tableName = command.getTable();
        WritePayload payload = (WritePayload) command.getPayload();

        TableService tableService = TableService.getInstance();

        Table table = tableService.find(tableName);
        if (table == null) {
            table = tableService.create(tableName);
        }

        table.write(payload.getKey(), payload.getValue());

        Response response = new Response();
        response.setResponseType(ResponseType.SUCCESS);
        return response;
    }
}
