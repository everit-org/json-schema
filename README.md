# JSON Schema Validator <a href="https://www.paypal.me/erosb88"><img style="float: right" width="70" height="35" src="./donate.jpg" title="Support this project by making a donation"></a>

## Deprecation notice

This library is currently in maintenance mode and superseded by [erosb/json-sKema](https://github.com/erosb/json-sKema).

This repository and won't see any new features. It provides solid support for draft-04, draft-06 and draft-07 versions of the JSON Schema specification.

The latest draft 2020-12 is supported only by [erosb/json-sKema](https://github.com/erosb/json-sKema).


[![Apache 2.0 License][ASL 2.0 badge]][ASL 2.0] [![Build Status][Travis badge master]][Travis] [![Coverage Status][Coveralls.io badge master]][Coveralls.io] 

* [When to use this library?](#when-to-use-this-library)
* [Maven installation](#maven-installation)
  * [Java7 version](#java67-versions)
* [Quickstart](#quickstart)
* [Draft 4, Draft 6 or Draft 7?](#draft-4-draft-6-or-draft-7)
* [Investigating failures](#investigating-failures)
  * [JSON report of the failures](#json-report-of-the-failures)
* [ValidationListeners - Tracking the validation process](#validationlisteners---tracking-the-validation-process)
* [Early failure mode](#early-failure-mode)
* [Lenient mode](#lenient-mode)
* [Default values](#default-values)
* [RegExp implementations](#regexp-implementations)
* [readOnly and writeOnly context](#readonly-and-writeonly-context)
* [Format validators](#format-validators)
  * [Example](#example)
* [$ref resolution](#ref-resolution)
* [Excluding dependencies](#excluding-dependencies)
* [Javadoc](#javadoc)

This project is an implementation of the JSON Schema [Draft v4][draft-zyp-json-schema-04], [Draft v6](https://tools.ietf.org/html/draft-wright-json-schema-01) and [Draft v7](https://tools.ietf.org/html/draft-handrews-json-schema-validation-00) specifications.
It uses the [org.json API](http://stleary.github.io/JSON-java/) (created by Douglas Crockford) for representing JSON data.

# When to use this library?

Lets assume that you already know what JSON Schema is, and you want to utilize it in a Java application to validate JSON data.
But - as you may have already discovered - there is also an [other Java implementation][java-json-tools/json-schema-validator]
of the JSON Schema specification. So here are some advices about which one to use:
 * if you use Jackson to handle JSON in Java code, then [java-json-tools/json-schema-validator] is obviously a better choice, since it uses Jackson
 * if you want to use the [org.json API](http://stleary.github.io/JSON-java/) then this library is the better choice
 * if you need JSON Schema Draft 6 / 7 support, then you need this library.
 * if you want to use anything else for handling JSON (like GSON or javax.json), then you are in a little trouble, since
currently there is no schema validation library backed by these libraries. It means that you will have to parse the JSON
twice: once for the schema validator, and once for your own processing. In a case like that, this library is probably still
a better choice, since it seems to be [twice faster](https://github.com/erosb/json-schema-perftest) than the Jackson-based [java-json-tools][java-json-tools/json-schema-validator] library.


## Maven installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
	<groupId>com.github.erosb</groupId>
	<artifactId>everit-json-schema</artifactId>
	<version>1.14.4</version>
</dependency>
```

_Note about older versions_: versions between `1.6.0` and `1.9.1` can only be found on [JitPack](https://jitpack.io/docs/) with `com.github.everit-org.json-schema:org.everit.json.schema` coordinates.  Versions `1.0.0` ... `1.5.1` are available on Maven Central under `org.everit.json:org.everit.json.schema` coordinates.

### Java6/7 versions

There were a couple of attempts to make the library work on Java 6/7.

A java6 port of version 1.9.2 was developed by @mindbender1 and it is accessible through Maven Central with the following coordinates:

```xml
    <dependency>
        <groupId>com.github.erosb</groupId>
        <artifactId>everit-json-schema-jdk6</artifactId>
        <version>1.9.2</version>
    </dependency>
```

Backports of older versions:
 * version 1.4.1 was backported by [Doctusoft](https://doctusoft.com/) with coordinates `com.doctusoft:json-schema-java7:1.4.1`
 * version 1.1.1 was backported by @rdruilhe and is available on JitPack as `com.github.rdruilhe.json-schema:org.everit.json.schema:1.1.1`

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

## Draft 4, Draft 6 or Draft 7?

JSON Schema has currently 4 major releases, Draft 3, Draft 4, Draft 6 and Draft 7. This library implements the 3 newer ones, you can have a quick look at the differences [here](https://github.com/json-schema-org/json-schema-spec/wiki/FAQ:-draft-wright-json-schema%5B-validation%5D-01#changes) and [here](https://tools.ietf.org/html/draft-handrews-json-schema-validation-00#appendix-B).
Since the two versions have a number of differences - and draft 6 is not backwards-compatible with draft 4 - it is good to know which version will you use.   

The best way to denote the JSON Schema version you want to use is to include its meta-schema URL in the document root with the `"$schema"` key. This is a common notation, facilitated by the library to determine which version should be used.

Quick reference:
  * if there is `"$schema": "http://json-schema.org/draft-04/schema"` in the schema root, then Draft 4 will be used
  * if there is `"$schema": "http://json-schema.org/draft-06/schema"` in the schema root, then Draft 6 will be used
  * if there is `"$schema": "http://json-schema.org/draft-07/schema"` in the schema root, then Draft 7 will be used
  * if none of these is found then Draft 4 will be assumed as default

If you want to specify the meta-schema version explicitly then you can change the default from Draft 4 to Draft 6 / 7 by configuring the loader this way:

```java
SchemaLoader loader = SchemaLoader.builder()
                .schemaJson(yourSchemaJSON)
                .draftV6Support() // or draftV7Support()
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


Now - to illustrate the way that multiple violations are handled - let's consider the following JSON document, where both
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

 * `"message"`: the programmer-friendly exception message (description of the validation failure)
 * `"keyword"`: the JSON Schema keyword which was violated
 * `"pointerToViolation"`: a JSON Pointer denoting the path from the input document root to its fragment which caused
 the validation failure
 * `"schemaLocation"`: a JSON Pointer denoting the path from the schema JSON root to the violated keyword
 * `"causingExceptions"`: a (possibly empty) array of sub-exceptions. Each sub-exception is represented as a JSON object,
 with the same structure as described in this listing. See more above about causing exceptions.

Please take into account that the complete failure report is a *hierarchical tree structure*: sub-causes of a cause can
be obtained using `#getCausingExceptions()` .

## ValidationListeners - Tracking the validation process

`ValidationListener`s can serve the purpose of resolving ambiguity about _how_ does an instance JSON match (or does not match)
against a schema. You can attach a `ValidationListener` implementation to the validator to receive event notifications about intermediate
success/failure results. 

Example:

```java
import org.everit.json.schema.Validator;
...
Validator validator = Validator.builder()
	.withListener(new YourValidationListenerImplementation())
	.build();
validator.performValidation(schema, input);
```

The currently supported events:

 * a `"$ref"` reference being resolved
 * a subschema under an `"allOf"` / `"anyOf"` / `"oneOf"` schema matching
 * a subschema under an `"allOf"` / `"anyOf"` / `"oneOf"` schema failing to match
 * an `"if"` schema matching
 * an `"if"` schema failing to match
 * an `"then"` schema matching
 * an `"then"` schema failing to match
 * an `"else"` schema matching
 * an `"else"` schema failing to match
 

See the javadoc of the `org.everit.json.schema.event.ValidationListener` interface for more details. The particular event classes also have
proper `#toJSON()` and `#toString()` implementations so you can print them in an easily parse-able format.

## Early failure mode

By default the validation error reporting in collecting mode (see the "Investigating failures" chapter). That is convenient for having a
detailed error report, but under some circumstances it is more appropriate to stop the validation when a failure is found without
checking the rest of the JSON document. To toggle this fast-failing validation mode
 * you have to explicitly build a `Validator` instance for your schema instead of calling `Schema#validate(input)`
 * you have to call the `failEarly()` method of `ValidatorBuilder`

Example:

```java
import org.everit.json.schema.Validator;
...
Validator validator = Validator.builder()
	.failEarly()
	.build();
validator.performValidation(schema, input);
```

_Note: the `Validator` class is immutable and thread-safe, so you don't have to create a new one for each validation, it is enough
to configure it only once._

## Lenient mode

In some cases, when validating numbers or booleans, it makes sense to accept string values that are parseable as such primitives, because
any successive processing will also automatically parse these literals into proper numeric and logical values. Also, non-string primitive values are trivial to convert to strings, so why not to permit any json primitives as strings?

For example, let's take this schema:

```json
{
    "properties": {
        "booleanProp": {
            "type": "boolean"
        },
        "integerProp": {
            "type": "integer"
        },
        "nullProp": {
            "type": "null"
        },
        "numberProp": {
            "type": "number"
        },
        "stringProp": {
          "type": "string"
        }
    }
}
```

The following JSON document fails to validate, although all of the strings could easily be converted into appropriate values:

```json
{
  "numberProp": "12.34",
  "integerProp": "12",
  "booleanProp": "true",
  "nullProp": "null",
  "stringProp": 12.34
}
```

In this case, if you want the above instance to pass the validation against the schema, you need to use the lenient primitive validation configuration turned on. Example:


```java
import org.everit.json.schema.*;
...
Validator validator = Validator.builder()
	.primitiveValidationStrategry(PrimitiveValidationStrategy.LENIENT)
	.build();
validator.performValidation(schema, input);
```

_Note: in lenient parsing mode, [all 22 possible boolean literals](https://yaml.org/type/bool.html) will be accepted as logical values._



## Default values

The JSON Schema specification defines the "default" keyword for denoting default values, though it doesn't explicitly state how it should
affect the validation process. By default this library doesn't set the default values, but if you need this feature, you can turn it on
by the `SchemaLoaderBuilder#useDefaults(boolean)` method, before loading the schema:

```json
{
  "properties": {
    "prop": {
      "type": "number",
      "default": 1
    }
  }
}
```


```java
JSONObject input = new JSONObject("{}");
System.out.println(input.get("prop")); // prints null
Schema schema = SchemaLoader.builder()
	.useDefaults(true)
	.schemaJson(rawSchema)
	.build()
	.load().build();
schema.validate(input);
System.out.println(input.get("prop")); // prints 1
```

If there are some properties missing from `input` which have `"default"` values in the schema, then they will be set by the validator
during validation.

## RegExp Implementations

For supporting the `"regex"` keyword of JSON Schema the library offers two possible implementations:
 * the default is based on the `java.util.regex` package
 * the other one is based on the [RE2J](https://github.com/google/re2j) library

While the RE2J library provides significantly better performance than `java.util.regex`, it is not completely compatible with
the syntax supported by `java.util` or ECMA 262. So RE2J is recommended if you are concerned about performance and its limitations are acceptable.

The RE2J implementation can be activated with the `SchemaLoaderBuilder#regexpFactory()` call:

```java
SchemaLoader loader = SchemaLoader.builder()
    .regexpFactory(new RE2JRegexpFactory())
    // ...
    .build();
```
Notes:
 - if you don't need the RE2J implementation, it is recommended to exclude it in your `pom.xml` so it doesn't increase your artifact's size unnecessarily
 - version history: in versions 1.0.0 ... 1.7.0 the `java.util` implementation was used, in 1.8.0 the RE2J implementation was used, and in 1.9.0 we made it
configurable, due to some reported regressions.


## readOnly and writeOnly context

The library supports the `readOnly` and `writeOnly` keywords which first appeared in Draft 7. If you want to utilize this feature, then before validation you need to tell the validator if the
validation happens in read or write context. Example:

schema.json:

```json
{
   "properties": {
     "id": {
       "type": "number",
       "readOnly": true
     }
   }  
}
```

Validation code snippet:
```java

Validator validator = Validator.builder()
                .readWriteContext(ReadWriteContext.WRITE)
                .build();

validator.performValidation(schema, new JSONObject("{\"id\":42}"));
```

In this case we told the validator that the validation happens in `WRITE` context, and in the input JSON object the `"id"` property appears, which is marked as `"readOnly"` in the schema, therefore this call will throw a `ValidationException`.

## Format validators


Starting from version `1.2.0` the library supports the [`"format"` keyword][draft-fge-json-schema-validation-00 format]
(which is an optional part of the specification).

The supported formats vary depending on the schema spec version you use (since the standard formats were introduced in different versions on the validation specification).

Here is a compatibility table of the supported standard formats:

|                      | Draft 4            | Draft 6            | Draft 7            |
|----------------------|--------------------|--------------------|--------------------|
| date-time            | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| email                | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| hostname             | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| ipv4                 | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| ipv6                 | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| uri                  | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| uri-reference        |                    | :white_check_mark: | :white_check_mark: |
| uri-template         |                    | :white_check_mark: | :white_check_mark: |
| json-pointer         |                    | :white_check_mark: | :white_check_mark: |
| date                 |                    |                    | :white_check_mark: |
| time                 |                    |                    | :white_check_mark: |
| regex                |                    |                    | :white_check_mark: |
| relative-json-pointer|                    |                    | :white_check_mark: |


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
schema.validate(jsonDocument);  // the document validation happens here
```

### Custom Provider Validators
The library use `DefaultProviderValidators` that collects all the validators used in the process of validation of json.
To use a custom Provider of Validators basically you:

 * create your own ProviderValidators in a class implementing the `org.everit.json.schema.loader.ProviderValidators` interface
 * bind your ProviderValidators in a `org.everit.json.schema.loader.SchemaLoader.SchemaLoaderBuilder.builder(IstanceOfNewProviderValidators` instance before loading the actual schema

#### Example for validate a field with a function javascript

Assume a schema with a simple function of validation that return true if the subject is equals to number 5:
```java
....
  "age": {
    ...
    "format": "javascript: subject == 5"
  },
...
```

Let's create a custom ProviderValidators that catch the intent of launch a javascript function and istantiate a new custom validator
```java
class ExampleDefaultProviderValidators extends DefaultProviderValidators {
    @Override
    public FormatValidator getFormatValidator(String formatName) {

        if (!this.getFormatValidators().containsKey(formatName)
                && formatName.startsWith("javascript:")) {
            String script = formatName.substring(formatName.lastIndexOf("javascript:"),formatName.length());
            this.addFormatValidator(formatName, new JavascriptFormatValidator(formatName, script));
        }
        return super.getFormatValidator(formatName);
    }
}

class JavascriptFormatValidator implements FormatValidator {

    String script;
    String formatName;

    public JavascriptFormatValidator(String formatName, String script) {
        this.formatName = formatName;
        this.script = script;
    }

    @Override
    public Optional<String> validate(String subject) {

        ScriptEngine javaScriptEngine = new ScriptEngineManager().getEngineByName("js");
        javaScriptEngine.put("subject",subject);
        try {
            Boolean result = (Boolean) javaScriptEngine.eval(script);
            if (!result) {
                return Optional.of(String.format("the length of string [%s] is not equal 5", subject));
            }
        } catch (ScriptException e) {
            e.printStackTrace();
            return Optional.of(String.format("Error on evalutation of [%s] ", subject));
        }
        return Optional.empty();
    }

    @Override
    public String formatName() {
        return formatName;
    }
}
```

In the builder we can set this new ProviderValidators and launch the validation process.
```java
ExampleDefaultProviderValidators customProviderValidators = new ExampleDefaultProviderValidators();
SchemaLoader schemaLoader = SchemaLoader.builder(customProviderValidators)
	.......
	.build();
Schema schema = schemaLoader.load().build(); // the schema is created using the above created configuration
schema.validate(jsonDocument);  // the document validation happens here
```


## $ref resolution

In a JSON Schema document it is possible to use relative URIs to refer previously defined
types. Such references are expressed using the `"$ref"` and `"$id"` keywords. While the specification describes resolution scope alteration and dereferencing in detail, it doesn't explain the expected behavior when the first occurring `"$ref"` or `"$id"` is a relative URI.

In the case of this implementation it is possible to explicitly define an absolute URI serving as the base URI (resolution scope) using the appropriate builder method:

```java
SchemaLoader schemaLoader = SchemaLoader.builder()
        .schemaJson(jsonSchema)
        .resolutionScope("http://example.org/") // setting the default resolution scope
        .build();
```

### Loading from the classpath

As your schemas grow you will want to split that up into multiple source files and wire them with `"$ref"` references.
If you want to store the schemas on the classpath (instead of eg. serving them through HTTP) then the recommended way is
to use the `classpath:` protocol to make the schemas reference each other. To make the `classpath:` protocol work:
 * if you use the [Spring framework](https://spring.io) you don't have to do anything, spring installs the necessary
 protocol handler out of the box
 * otherwise you can utilize the library's built-in classpath-aware `SchemaClient`, example:

```java
SchemaLoader schemaLoader = SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .schemaJson(jsonSchema)
        .resolutionScope("classpath://my/schemas/directory/") // setting the default resolution scope
        .build();
```

Given this configuration, the following references will be properly resolved in `jsonSchema`:

```json
{
    "properties": {
        "sameDir": { "$ref": "sameDirSchema.json" },
        "absPath": { "$ref": "classpath://somewhere/else/otherschema.json" },
        "httpPath": { "$ref": "http://example.org/http-works-as-usual" },
    }
}
```

and `sameDirSchema.json` will be looked for in `/my/schemas/directory/sameDirSchema.json` on the classpath.

### Registering schemas by URI

Sometimes it is useful to work with preloaded schemas, to which we assign an arbitary URI (maybe an uuid) instead of
loading the schema through a URL. This can be done by assigning the schemas to a URI with the `#registerSchemaByURI()` 
method of the schema loader. Example:

```java
SchemaLoader schemaLoader = SchemaLoader.builder()
        .registerSchemaByURI(new URI("urn:uuid:a773c7a2-1a13-4f6a-a70d-694befe0ce63"), aJSONObject)
        .registerSchemaByURI(new URI("http://example.org"), otherJSONObject)
        .schemaJson(jsonSchema)
        .resolutionScope("classpath://my/schemas/directory/")
        .build();
```

Notes: 
 * the passed schema object must be a `JSONObject` or a `Boolean` (the formal parameter type is `Object` only because
 these two don't have any other common superclass).
 * if you want, you can pass a URL with HTTP protocol, it is still a valid URI. Since in this case you pre-assigned a schema
 to a URI, there will be no network call made. This can be a caching strategy (though defining your own `SchemaClient`
 implementation works too, or you can even utilize the extensible [protocol handling](https://stackoverflow.com/questions/26363573/registering-and-using-a-custom-java-net-url-protocol) of the `java.net` package)

## Excluding dependencies

Some of the dependencies can be excluded from the library, and it still remains usable, with some limitations:
 * if you exclude the `com.damnhandy:handy-uri-templates` dependency, then your schema shouldn't use the `"uri-template"` format
 * if you exclude the `commons-validator:commons-validator` dependency, then your schema shouldn't use the following formats: `"email"`, `"ipv4"`, `"ipv6"`, `"hostname"`


## Javadoc

By library version:

 * [1.12.2](http://erosb.github.io/everit-json-schema/javadoc/1.12.2/)
 * [1.12.1](http://erosb.github.io/everit-json-schema/javadoc/1.12.1/)
 * [1.12.0](http://erosb.github.io/everit-json-schema/javadoc/1.12.0/)
 * [1.11.1](http://erosb.github.io/everit-json-schema/javadoc/1.11.1/)
 * [1.11.0](http://erosb.github.io/everit-json-schema/javadoc/1.11.0/)
 * [1.10.0](http://erosb.github.io/everit-json-schema/javadoc/1.10.0/)
 * [1.9.2](http://erosb.github.io/everit-json-schema/javadoc/1.9.2/)

The generated javadoc of versions 1.0.0 - 1.5.1 is available at [javadoc.io](http://javadoc.io/doc/org.everit.json/org.everit.json.schema/1.5.1)

For the versions in between (1.6.0 - 1.9.1) it isn't published anywhere.



[ASL 2.0 badge]: https://img.shields.io/:license-Apache%202.0-blue.svg
[ASL 2.0]: https://www.apache.org/licenses/LICENSE-2.0
[Travis badge master]: https://travis-ci.org/everit-org/json-schema.svg?branch=master
[Travis]: https://travis-ci.org/everit-org/json-schema
[Coveralls.io badge master]: https://coveralls.io/repos/github/everit-org/json-schema/badge.svg?branch=master
[Coveralls.io]: https://coveralls.io/github/everit-org/json-schema?branch=master
[java-json-tools/json-schema-validator]: https://github.com/java-json-tools/json-schema-validator
[draft-zyp-json-schema-04]: https://tools.ietf.org/html/draft-zyp-json-schema-04
[draft-fge-json-schema-validation-00 format]: https://tools.ietf.org/html/draft-fge-json-schema-validation-00#section-7

