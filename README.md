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
    <version>1.1.0</version>
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

Investigating failures
----------------------

Starting from version `1.1.0` the validator collects every schema violations (instead of failing immediately on the first
one). Each failure is denoted by a JSON pointer, pointing from the root of the document to the violating part. If  more
than one schema violations have been detected, then a `ValidationException` will be thrown at the most common parent
elements of the violations, and each separate violations can be obtained using the `ValidationException#getCausingExceptions()`
method.

To demonstrate the above concepts, lets see an example. Lets consider the following schema:

```json
{
	"type" : "object",
	"properties" : {
		"rectangle" : {"$ref" : "#/definitions/Rectangle" }
	},
	"definitions" : {
		"size" : {
			"type" : "number",
			"minimum" : 0
		},
		"Rectangle" : {
			"type" : "object",
			"properties" : {
				"a" : {"$ref" : "#/definitions/size"},
				"b" : {"$ref" : "#/definitions/size"}
			}
		}
	}
}
```

The following JSON document has only one violation against the schema (since "a" cannot be negative):

```json
{
	"rectangle" : {
		"a" : -5,
		"b" : 5
	}
}
```

In this case the thrown `ValidationException` will point to `#/rectangle/a` and it won't contain sub-exceptions:

```java
try {
  schema.validate(rectangleSingleFailure);
} catch (ValidationException e) {
  // prints #/rectangle/a: -5.0 is not higher or equal to 0
  System.out.println(e.getMessage()); 
}
```


Now - to illustrate the way how multiple violations are handled - lets consider the following JSON document, where both
the "a" and "b" properties violate the above schema:

```json
{
	"rectangle" : {
		"a" : -5,
		"b" : "asd"
	}
}
```

In this case the thrown `ValidationException` will point to `#/rectangle`, and it has 2 sub-exceptions, pointing to
`#/rectangle/a` and `#/rectangle/b` :

```java
try {
  schema.validate(rectangleMultipleFailures);
} catch (ValidationException e) {
  System.out.println(e.getMessage());
  e.getCausingExceptions().stream()
      .map(ValidationException::getMessage)
      .forEach(System.out::println);
}
```

This will print the following output:
```
#/rectangle: 2 schema violations found
#/rectangle/a: -5.0 is not higher or equal to 0
#/rectangle/b: expected type: Number, found: String
```

