package poc;

import dobby.util.Json;
import janus.DataClass;
import janus.annotations.JanusDataClass;
import janus.annotations.JanusList;
import janus.annotations.JanusString;

import java.util.List;
import java.util.stream.Collectors;

public class TestClass implements DataClass {
    @JanusString("name")
    private String name;

    @JanusDataClass("testClass2")
    private TestClass2 testClass2;

    @JanusList("list")
    private List<TestClass2> list;

    public void setName(String name) {
        this.name = name;
    }

    public void setTestClass2(TestClass2 testClass2) {
        this.testClass2 = testClass2;
    }

    public void setList(List<TestClass2> list) {
        this.list = list;
    }

    @Override
    public String getKey() {
        return null;
    }

    public Json toJson() {
        final Json json = new Json();
        json.setString("name", name);
        json.setJson("testClass2", testClass2.toJson());
        json.setList("list", list.stream().map(TestClass2::toJson).collect(Collectors.toList()));

        return json;
    }
}
