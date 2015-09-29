JSON Schema Validator
=====================

This project is an implementation of the [JSON Schema Core Draft v4](http://json-schema.org/latest/json-schema-core.html) specification.
It uses the [org.json API](http://www.json.org/java/) for representing JSON data.

Quickstart
----------

```java
import org.everit.jsonvalidator.Schema;
import org.everit.jsonvalidator.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
// ...
try (InputStream inputStream = getClass().getResourceAsStream("/path/to/your/schema.json")) {
  JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
  Schema schema = SchemaLoader.load(rawSchema);
  schema.validate(new JSONObject("{\"hello\" : \"world\"}")); // throws a ValidationException if this object is invalid
}
```


