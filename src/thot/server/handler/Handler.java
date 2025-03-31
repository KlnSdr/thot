package thot.server.handler;

import thot.api.command.Command;
import thot.api.response.Response;

public interface Handler {
    Response handle(Command command);
}
