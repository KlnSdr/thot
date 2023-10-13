package thot.server.handler;

import thot.common.command.Command;
import thot.common.response.Response;

public interface Handler {
    Response handle(Command command);
}
