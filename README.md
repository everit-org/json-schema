JSON Schema Validator
=====================

This project is an implementation of the [JSON Schema Core Draft v4](http://json-schema.org/latest/json-schema-core.html) specification.
It uses the [org.json API](http://www.json.org/java/) for representing JSON data.

Maven installation
------------------
Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>org.everit.json</groupId>
    <artifactId>org.everit.json.schema</artifactId>
    <version>1.0.0</version>
</dependency>
```

Quickstart
----------

```java
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
// ...
try (InputStream inputStream = getClass().getResourceAsStream("/path/to/your/schema.json")) {
  JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
  Schema schema = SchemaLoader.load(rawSchema);
  schema.validate(new JSONObject("{\"hello\" : \"world\"}")); // throws a ValidationException if this object is invalid
}
```


