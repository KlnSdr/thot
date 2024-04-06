package poc;

import dobby.util.Json;
import janus.Janus;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        final TestClass test = new TestClass();
        final TestClass2 test2 = new TestClass2();

        test.setName("John Doe");
        test2.setAge(25);
        test2.setDisplayName("jonny");
        test.setTestClass2(test2);

        final ArrayList<TestClass2> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final TestClass2 testClass2 = new TestClass2();
            testClass2.setAge(i);
            testClass2.setDisplayName("name" + i);
            list.add(testClass2);
        }
        test.setList(list);

        final Json testJson = test.toJson();

        final TestClass parsed = Janus.parse(testJson, TestClass.class);

        assert parsed != null;
        System.out.println(parsed.toJson().toString());
    }
}
