package poc;

import dobby.util.Json;
import janus.annotations.JanusString;

public class TestClass {
    @JanusString("name")
    private String name;

    public TestClass() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public Json toJson() {
        final Json json = new Json();
        json.setString("name", name);
        return json;
    }
}
