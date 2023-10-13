package thot;

import thot.server.Server;
import thot.tables.service.TableService;

public class Main {
    public static void main(String[] args) {
        TableService.getInstance();

        Server.startApplication();
    }
}
