# Thot Object Store

Thot is a simple Object store based on the [Dobby](https://github.com/klnsdr/Dobby) Webserver.
Objects are stored in tables called *buckets* and need to implement the *Serializable* Interface. Because Thot needs 
access to the projects classes it needs to be included in the project and packaged in the resulting jar. Starting 
the Thot Store is done by running the following command `java -cp <application>.jar thot.Main` ~One central Thot 
instance for multiple projects is not possible (yet).~ Using the same Thot instance for multiple projects is now 
possible by using the class `janus.Janus` to parse stored Json to DataClass-Objects. For this to work the classes 
need to have the respective annotations on all relevant fields.

## connecting to Thot
To make a request to a running Thot instance simple call the appropriate method of `thot.connector.Connector`

### writing Data
The write method of the connector returns a boolean indicating if the transaction was successful.
```java
import thot.connector.Connector;
// ...
final String bucketName = "userBucket";
final User user = new User(...); // User implements Serializable
boolean didWrite = Connector.write(bucketName, user.getKey(), user);
```

### reading Data
#### read by key
```java
import thot.connector.Connector;
//...
final String key = "someKey";
final String bucketName = "userBucket";
final User user = Connector.read(bucketName, key, User.class);
```

#### read by pattern
```java
import thot.connector.Connector;
//...
final String partialKey = "some.*"; // regex!
final String bucketName = "userBucket";
final User[] users = Connector.read(bucketName, partialKey, User.class);
```

## deleting Data
```java
import thot.connector.Connector;
//...
final String key = "someKey";
final String bucketName = "userBucket";
boolean didDelete = Connector.delete(bucketName, key);
```

## reconstruct objects from json
TestObject.class

```java
import dobby.util.Json;
import janus.DataClass;
import janus.annotations.JanusString;

class TestObject implements DataClass {
    @JanusString("name")
    private String name;

    @JanusInteger("randomNumbers")
    private List<Integer> randomNumbers;

    public Json toJson() {
        final Json json = new Json();
        json.setString("name", name);
        json.setList("randomNumbers", randomNumbers.stream().map(val -> (Object) val).collect(Collectors.toList()));
        return json;
    }
    
    public String getKey() {
        // ...
        return key;
    }
}
```

someResource.java
```java
// ...
boolean save(TestClass value) {
    return Connector.write("testBucket", value.getKey(), value.toJson());
}

TestClass find(String key) {
    final Json json = Connector.read("testBucket", key, Jsonc.class);
    return Janus.parse(json, TestClass.class);
}
// ...
```
