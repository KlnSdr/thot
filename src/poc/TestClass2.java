package poc;

import dobby.util.Json;
import janus.DataClass;
import janus.annotations.JanusInteger;
import janus.annotations.JanusString;

public class TestClass2 implements DataClass {
    @JanusInteger("age")
    private int age;
    @JanusString("displayName")
    private String displayName;

    public void setAge(int age) {
        this.age = age;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public Json toJson() {
        final Json json = new Json();
        json.setInt("age", age);
        json.setString("displayName", displayName);
        return json;
    }
}
