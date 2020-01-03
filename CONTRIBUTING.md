# Contributing guidelines


## Submitting bugreports


Though any kind of feedback, feature request and bugreport is appreciated, I would more prefer if you would send a proper
pull request with a failing test in case you find a bug in the library.

Currently there is a simple mechanism in the integration tests to make it easy to add new tests. Using this is especially
preferred if your bugreport is related to a complex schema or multiple schemas. There is no need to dig into the java code,
you only have to create a few new files in the repo (but you will have to run the tests - see the build instructions below).

### Steps:
 * create an issue, just to get an issue number
 * fork the repository
 * in your fork, create a directory under the `tests/vanilla/src/main/resources/org/everit/json/schema/issues/` directory (for example `issue42` )
 * in this directory create a `schema.json` file with your JSON Schema document that is not handled correctly
 * in the same directory create a `subject-valid.json` file, which is a JSON document, and you expect that document to pass
the validation, but due to a bug it fails with a `ValidationException`
 * if you have a JSON document that you expect to be invalid, but it passes the validation, then you should name this file `subject-invalid.json`.
It will mean that for the test suite that an expected `ValidationException` is not thrown.
 * you can create both the `subject-valid.json` and `subject-invalid.json` test files if you find it needed


### Remote schema loading:
If your testcase has anything to do with remote schemas, then
 * you can put those schemas under the `yourIssueDir/remotes/` directory
 * the `yourIssueDir/remotes/` directory will act as the document root of a HTTP server during test execution
 * this HTTP server will listen at address `http://localhost:1234` so please change your schemas (`id` and `$ref` properties)
to fetch the remote schemas relative from this address

You can find a good example for all of these in the `tests/vanilla/src/main/resources/org/everit/json/schema/issues/issue17` testcase.

If you successfully created your testcase, then it will fail with an `AssertionError` with a message like
"validation failed with: org.everit.json.schema.ValidationException:..." or "did not throw ValidationException for invalid subject",
and then you are ready to send a pull request.

### Defining expected failures in integration tests

In some cases the error you want to report is not about "if a given json is valid against a schema", but instead an inproper validation failure report to be tested. In such case you can create a testcase which contains:
* a `schema.json`
* a `subject-invalid.json`
* an `expected-exception.json`

The last one should contain a JSON description of the expected exception. Such an exception description has at most 2 keys:
* `message`: (required) the value is the expected exception message
* `causingExceptions` : (optional) the value is an array of causing exceptions (which are returned by the `ġetCausingExceptions()` method of the expected `ValidationException`). Each item of this entry should also be an exception description, so these are objects with a required `message` and an optional `causingExceptions` key. Example `expected-exception.json`:

```js
{
  "message": "#: 4 schema violations found",
  "causingExceptions": [
     {
        "message": "#/0: 2 schema violations found",
        "causingExceptions": [
           {
              "message": "#/0/name: expected type: String, found: JSONArray"
           },
           {
              "message": "#/0/dimensions/width: expected type: Number, found: String"
           }
        ]
     },
     {
        "message": "#/1: required key [price] not found"
     },
     {
        "message": "#/2/id: expected type: Number, found: String"
     }
  ]
}
```

### Setting validator configuration for integration tests

Aside the above listed files you can also specify a `validator-config.json` file containing information for the testrunner 
about how to configure the loader and validator objects while running your test. In most cases you will be fine with the 
defaults but in some cases you might need to change defaults. This can contain the following keys (all
of them are optional):

`validator-config.json` reference:
 * `"failEarly"` (boolean): if set to `true` the validator will run in early failing mode (`false` by default)
 * `"resolutionScope"` : sets the initial resolution scope (implicit top-level `"$id"`)
 * `"regexpImplementation"`: if set to `"RE2J"` the validator will use the [RE2J](https://github.com/google/re2j) -based
 regexp implementation (otherwise the default `java.util.regex` package is used.
 * `"customFormats"` : a json object, each key is a format name, and the value is the fully qualified name of a format class
 (implementing `org.everit.json.schema.FormatValidator`) you need to use in your testcase. The class must have a default
 constructor. If you utilize this setting then you will also have to include the format class in your PR so that the testrunner
 can instantiate it.
 * `"metaSchemaVersion"`: can be `4` or `6` or `7`. Setting it to `6` or `7` enables draft-6 and draft-7 support respectively.
 * `"enableOverrideOfBuiltInFormatValidators"`: set to true if you dont want the library overrides your custom format validators.


## Building the project locally

Prerequisities: the following tools have to be installed:
* jdk1.8.0_45 (earlier versions of javac cannot compile the project due to a type inference issue)
* maven 3.x


Steps for building the project:
* clone the repository: `git clone https://github.com/everit-org/json-schema.git && cd json-schema/`
* build it with maven: `mvn clean install`

(or just `mvn clean test` if you are only interested in running the tests)


## Running mutation testing (pitest)

`mvn org.pitest:pitest-maven:mutationCoverage`
