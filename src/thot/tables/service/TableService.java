package thot.tables.service;

import thot.tables.Table;

import java.util.concurrent.ConcurrentHashMap;

public class TableService {
    private static TableService instance;
    private final ConcurrentHashMap<String, Table> tables;

    private TableService() {
        this.tables = new ConcurrentHashMap<>();
        loadTablesFromDisk();
    }

    public static TableService getInstance() {
        if (instance == null) {
            instance = new TableService();
        }
        return instance;
    }

    public Table find(String name) {
        return this.tables.get(name);
    }

    public Table create(String name) {
        if (this.tables.containsKey(name)) {
            throw new IllegalArgumentException("Table already exists");
        }
        this.tables.put(name, new Table());
        return find(name);
    }

    public void delete(String name) {
        if (!this.tables.containsKey(name)) {
            throw new IllegalArgumentException("Table does not exist");
        }
        this.tables.remove(name);
    }

    private void loadTablesFromDisk() {
        // todo implement
    }
}
