# JSON Schema Validator

[![Apache 2.0 License][ASL 2.0 badge]][ASL 2.0] [![Build Status][Travis badge master]][Travis] [![Coverage Status][Coveralls.io badge master]][Coveralls.io]

* [When to use this library?](#when-to-use-this-library)
* [Maven installation](#maven-installation)
  * [Java7 version](#java7-version)
* [Quickstart](#quickstart)
* [Draft 4 or Draft 6?](#draft-4-or-draft-6)
* [Investigating failures](#investigating-failures)
  * [JSON report of the failures](#json-report-of-the-failures)
* [Format validators](#format-validators)
  * [Example](#example)
* [Resolution scopes](#resolution-scopes)

<a href="http://jetbrains.com"><img src="./jetbrains-logo.png" /></a> Supported by JetBrains.

This project is an implementation of the JSON Schema [Draft v4][draft-zyp-json-schema-04] and [Draft v6](https://tools.ietf.org/html/draft-wright-json-schema-01) specifications.
It uses the [org.json API](http://stleary.github.io/JSON-java/) (created by Douglas Crockford) for representing JSON data.

# When to use this library?

Lets assume that you already know what JSON Schema is, and you want to utilize it in a Java application to validate JSON data.
But - as you may have already discovered - there is also an [other Java implementation][java-json-tools/json-schema-validator]
of the JSON Schema specification. So here are some advices about which one to use:
 * if you use Jackson to handle JSON in Java code, then [java-json-tools/json-schema-validator] is obviously a better choice, since it uses Jackson
 * if you want to use the [org.json API](http://stleary.github.io/JSON-java/) then this library is the better choice
 * if you need JSON Schema Draft 6 support, then you need this library.
 * if you want to use anything else for handling JSON (like GSON or javax.json), then you are in a little trouble, since
currently there is no schema validation library backed by these libraries. It means that you will have to parse the JSON
twice: once for the schema validator, and once for your own processing. In a case like that, this library is probably still
a better choice, since it seems to be [twice faster](https://github.com/erosb/json-schema-perftest) than the Jackson-based [java-json-tools][java-json-tools/json-schema-validator] library.


## Maven installation

Add the JitPack repository and the dependency to your `pom.xml` as follows:

```xml
<dependency>
    <groupId>com.github.everit-org.json-schema</groupId>
    <artifactId>org.everit.json.schema</artifactId>
    <version>1.6.1</version>
</dependency>
...
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

_Note_: from version `1.6.0`, the library is primarily distributed through JitPack. Previous versions are also available through maven central.

### Java7 version

If you are looking for a version which works on Java7, then you can use this artifact, kindly backported by [Doctusoft](https://doctusoft.com/):

```xml
<dependency>
    <groupId>com.doctusoft</groupId>
    <artifactId>json-schema-java7</artifactId>
    <version>1.4.1</version>
</dependency>
```

## Quickstart


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

## Draft 4 or draft 6?

JSON Schema has currently 3 major releases, Draft 3, Draft 4 and Draft 6. This library implements the 2 newer ones, you can have a quick look at the differences [here](https://github.com/json-schema-org/json-schema-spec/wiki/FAQ:-draft-wright-json-schema%5B-validation%5D-01#changes).
Since the two versions have a number of differences - and draft 6 is not backwards-compatible with draft 4 - it is good to know which version will you use.   

The best way to denote the JSON Schema version you want to use is to include its meta-schema URL in the document root with the `"$schema"` key. This is a common notation, facilitated by the library to determine which version should be used.

Quick reference:
  * if there is `"$schema": "http://json-schema.org/draft-04/schema"` in the schema root, then Draft 4 will be used
  * if there is `"$schema": "http://json-schema.org/draft-06/schema"` in the schema root, then Draft 6 will be used
  * if none of these is found then Draft 4 will be assumed as default

If you want to specify the meta-schema version explicitly then you can change the default from Draft 4 to Draft 6 by configuring the loader this way:

```java
SchemaLoader loader = SchemaLoader.builder()
                .schemaJson(yourSchemaJSON)
                .draftV6Support()
                .build();
Schema schema = loader.load().build();
```

## Investigating failures


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
### JSON report of the failures

Since version `1.4.0` it is possible to print the `ValidationException` instances as
JSON-formatted failure reports. The `ValidationException#toJSON()` method returns a `JSONObject` instance with the
following keys:

 * `"message"`: the programmer-friendly exception message (desription of the validation failure)
 * `"keyword"`: the JSON Schema keyword which was violated
 * `"pointerToViolation"`: a JSON Pointer denoting the path from the input document root to its fragment which caused
 the validation failure
 * `"schemaLocation"`: a JSON Pointer denoting the path from the schema JSON root to the violated keyword
 * `"causingExceptions"`: a (possibly empty) array of sub-exceptions. Each sub-exception is represented as a JSON object,
 with the same structure as described in this listing. See more above about causing exceptions.

Please take into account that the complete failure report is a *hierarchical tree structure*: sub-causes of a cause can
be obtained using `#getCausingExceptions()` .  


## Format validators


Starting from version `1.2.0` the library supports the [`"format"` keyword][draft-fge-json-schema-validation-00 format]
(which is an optional part of the specification), so you can use the following formats in the schemas:

 * date-time
 * email
 * hostname
 * ipv4
 * ipv6
 * uri

If you use the library in Draft 6 mode, then the followings are also supported:

 * uri-reference
 * uri-template
 * json-pointer  

The library also supports adding custom format validators. To use a custom validator basically you have to

 * create your own validation in a class implementing the `org.everit.json.schema.FormatValidator` interface
 * bind your validator to a name in a `org.everit.json.schema.loader.SchemaLoader.SchemaLoaderBuilder` instance before loading the actual schema

### Example



Lets assume the task is to create a custom validator which accepts strings with an even number of characters.

The custom `FormatValidator` will look something like this:

```java
public class EvenCharNumValidator implements FormatValidator {

  @Override
  public Optional<String> validate(final String subject) {
    if (subject.length() % 2 == 0) {
      return Optional.empty();
    } else {
      return Optional.of(String.format("the length of string [%s] is odd", subject));
    }
  }

}
```

To bind the `EvenCharNumValidator` to a `"format"` value (for example `"evenlength"`) you have to bind a validator instance
to the keyword in the schema loader configuration:

```java
JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
SchemaLoader schemaLoader = SchemaLoader.builder()
	.schemaJson(rawSchema) // rawSchema is the JSON representation of the schema utilizing the "evenlength" non-standard format
	.addFormatValidator("evenlength", new EvenCharNumValidator()) // the EvenCharNumValidator gets bound to the "evenlength" keyword
	.build();
Schema schema = schemaLoader.load().build(); // the schema is created using the above created configuration
schema.validate(jsonDcoument);  // the document validation happens here
```


## Resolution scopes

In a JSON Schema document it is possible to use relative URIs to refer previously defined
types. Such references are expressed using the `"$ref"` and `"id"` keywords. While the specification describes resolution scope alteration and dereferencing in detail, it doesn't explain the expected behavior when the first occuring `"$ref"` or `"id"` is a relative URI.

In the case of this implementation it is possible to explicitly define an absolute URI serving as the base URI (resolution scope) using the appropriate builder method:

```java
SchemaLoader schemaLoader = SchemaLoader.builder()
        .schemaJson(jsonSchema)
        .resolutionScope("http://example.org/") // setting the default resolution scope
        .build();
```

[ASL 2.0 badge]: https://img.shields.io/:license-Apache%202.0-blue.svg
[ASL 2.0]: https://www.apache.org/licenses/LICENSE-2.0
[Travis badge master]: https://travis-ci.org/everit-org/json-schema.svg?branch=master
[Travis]: https://travis-ci.org/everit-org/json-schema
[Coveralls.io badge master]: https://coveralls.io/repos/github/everit-org/json-schema/badge.svg?branch=master
[Coveralls.io]: https://coveralls.io/github/everit-org/json-schema?branch=master
[java-json-tools/json-schema-validator]: https://github.com/java-json-tools/json-schema-validator
[draft-zyp-json-schema-04]: https://tools.ietf.org/html/draft-zyp-json-schema-04
[draft-fge-json-schema-validation-00 format]: https://tools.ietf.org/html/draft-fge-json-schema-validation-00#section-7
