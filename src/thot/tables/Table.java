package thot.tables;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Table {
    private final ConcurrentHashMap<String, Serializable> table;

    public Table() {
        this.table = new ConcurrentHashMap<>();
    }

    public void write(String key, Serializable value) {
        this.table.put(key, value);
    }

    public Serializable read(String key) {
        return this.table.get(key);
    }

    public void delete(String key) {
        this.table.remove(key);
    }

    public boolean contains(String key) {
        return this.table.containsKey(key);
    }

    private void saveToDisk() {
        // todo implement
    }
}
