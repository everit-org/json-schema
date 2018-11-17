package org.everit.json.schema;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.regexp.RE2JRegexpFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class IssueTest {

    @Parameters(name = "{1}")
    public static List<Object[]> params() {
        List<Object[]> rval = new ArrayList<>();
        try {
            File issuesDir = new File(
                    IssueTest.class.getResource("/org/everit/json/schema/issues").toURI());
            for (File issue : issuesDir.listFiles()) {
                rval.add(new Object[] { issue, issue.getName() });
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return rval;
    }

    private final File issueDir;

    private JettyWrapper servletSupport;

    private List<String> validationFailureList;

    private List<String> expectedFailureList;

    private SchemaLoader.SchemaLoaderBuilder loaderBuilder;

    private Validator.ValidatorBuilder validatorBuilder = Validator.builder();

    public IssueTest(final File issueDir, final String ignored) {
        this.issueDir = requireNonNull(issueDir, "issueDir cannot be null");
    }

    private Optional<File> fileByName(final String fileName) {
        return Arrays.stream(issueDir.listFiles())
                .filter(file -> file.getName().equals(fileName))
                .findFirst();
    }

    private void initJetty(final File documentRoot) {
        try {
            servletSupport = new JettyWrapper(documentRoot);
            servletSupport.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JSONObject fileAsJson(File file) {
        try {
            return new JSONObject(new JSONTokener(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Schema loadSchema() {
        Optional<File> schemaFile = fileByName("schema.json");
        try {
            if (schemaFile.isPresent()) {
                JSONObject schemaObj = fileAsJson(schemaFile.get());
                loaderBuilder = SchemaLoader.builder().schemaJson(schemaObj);
                consumeValidatorConfig();
                return loaderBuilder.build().load().build();
            }
            throw new RuntimeException(issueDir.getCanonicalPath() + "/schema.json is not found");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void consumeValidatorConfig() {
        Map<String, Consumer<Object>> configKeyHandlers = new HashMap<>();
        configKeyHandlers.put("failEarly", value -> {
            if (Boolean.TRUE.equals(value)) {
                validatorBuilder.failEarly();
            }
        });
        configKeyHandlers.put("resolutionScope", value -> loaderBuilder.resolutionScope((String) value));
        configKeyHandlers.put("regexpImplementation", value -> {
            if (Objects.equals("RE2J", value)) {
                loaderBuilder.regexpFactory(new RE2JRegexpFactory());
            }
        });
        configKeyHandlers.put("customFormats", value -> {
            JSONObject json = (JSONObject) value;
            json.toMap().entrySet()
                    .forEach(entry -> loaderBuilder
                            .addFormatValidator(entry.getKey(), this.createFormatValidator(entry)));
        });
        configKeyHandlers.put("metaSchemaVersion", value -> {
            int versionNo = (Integer) value;
            if (!asList(4, 6, 7).contains(versionNo)) {
                throw new IllegalArgumentException(
                        "invalid metaSchemaVersion in validator-config.json: should be one of 4, 6, or 7, found: " + versionNo);
            }
            if (versionNo == 6) {
                loaderBuilder.draftV6Support();
            } else if (versionNo == 7) {
                loaderBuilder.draftV7Support();
            }
        });
        fileByName("validator-config.json").map(file -> fileAsJson(file)).ifPresent(configJson -> {
            configKeyHandlers.entrySet()
                    .stream()
                    .filter(entry -> configJson.has(entry.getKey()))
                    .forEach(entry -> entry.getValue().accept(configJson.get(entry.getKey())));
        });
    }

    private FormatValidator createFormatValidator(Map.Entry<String, Object> entry) {
        String formatClassName = (String) entry.getValue();
        try {
            Class<? extends FormatValidator> formatClass = (Class<? extends FormatValidator>) Class.forName(formatClassName);
            Constructor<? extends FormatValidator> ctor = formatClass.getConstructor();
            return ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stopJetty() {
        if (servletSupport != null) {
            servletSupport.stop();
        }
    }

    @Test
    public void test() {
        Assume.assumeFalse("issue dir starts with 'x' - ignoring", issueDir.getName().startsWith("x"));
        fileByName("remotes").ifPresent(this::initJetty);
        try {
            Schema schema = loadSchema();
            fileByName("subject-valid.json").ifPresent(file -> validate(file, schema, true));
            fileByName("subject-invalid.json").ifPresent(file -> validate(file, schema, false));
        } finally {
            stopJetty();
        }
    }

    private void validate(final File file, final Schema schema, final boolean shouldBeValid) {
        ValidationException thrown = null;

        Object subject = loadJsonFile(file);

        try {
            Validator validator = validatorBuilder.build();
            validator.performValidation(schema, subject);
        } catch (ValidationException e) {
            thrown = e;
        }

        if (shouldBeValid && thrown != null) {
            thrown.getAllMessages().forEach(System.out::println);
            StringBuilder failureBuilder = new StringBuilder("validation failed with: " + thrown);
            for (ValidationException e : thrown.getCausingExceptions()) {
                failureBuilder.append("\n\t").append(e.getMessage());
            }
            Assert.fail(failureBuilder.toString());
        }
        if (!shouldBeValid && thrown != null) {
            Optional<File> expectedFile = fileByName("expectedException.json");
            if (expectedFile.isPresent()) {
                if (!checkExpectedValues(expectedFile.get(), thrown)) {
                    expectedFailureList.stream()
                            .filter(exp -> !validationFailureList.contains(exp))
                            .forEach(System.out::println);
                    System.out.println("--");
                    validationFailureList.stream()
                            .filter(exp -> !expectedFailureList.contains(exp))
                            .forEach(System.out::println);
                    Assert.fail("Validation failures do not match expected values: \n" +
                            "Expected: " + expectedFailureList.stream().collect(joining("\n\t")) + ",\nActual:   " +
                            validationFailureList.stream().collect(joining("\n\t")));
                }
            }
        }
        if (!shouldBeValid && thrown == null) {
            Assert.fail("did not throw ValidationException for invalid subject");
        }
    }

    // TODO - it would be nice to see this moved out of tests to the main
    // source so that it can be used as a convenience method by users also...
    private Object loadJsonFile(final File file) {

        Object subject = null;

        try {
            JSONTokener jsonTok = new JSONTokener(new FileInputStream(file));

            // Determine if we have a single JSON object or an array of them
            Object jsonTest = jsonTok.nextValue();
            if (jsonTest instanceof JSONObject) {
                // The message contains a single JSON object
                subject = jsonTest;
            } else if (jsonTest instanceof JSONArray) {
                // The message contains a JSON array
                subject = jsonTest;
            }
        } catch (JSONException e) {
            throw new RuntimeException("failed to parse subject json file", e);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        return subject;
    }

    /**
     * Allow users to provide expected values for validation failures. This method reads and parses
     * files formatted like the following:
     * <p>
     * { "message": "#: 2 schema violations found", "causingExceptions": [ { "message": "#/0/name:
     * expected type: String, found: JSONArray", "causingExceptions": [] }, { "message": "#/1:
     * required key [price] not found", "causingExceptions": [] } ] }
     * <p>
     * The expected contents are then compared against the actual validation failures reported in the
     * ValidationException and nested causingExceptions.
     */
    private boolean checkExpectedValues(final File expectedExceptionsFile,
            final ValidationException ve) {
        // Read the expected values from user supplied file
        Object expected = loadJsonFile(expectedExceptionsFile);
        expectedFailureList = new ArrayList<String>();
        // NOTE: readExpectedValues() will update expectedFailureList
        readExpectedValues((JSONObject) expected);

        // Read the actual validation failures into a list
        validationFailureList = new ArrayList<>();
        // NOTE: processValidationFailures() will update validationFailureList
        processValidationFailures(ve);

        // Compare expected to actual
        return new HashSet<>(expectedFailureList).equals(new HashSet<>(validationFailureList));
    }

    // Recursively process the ValidationExceptions, which can contain lists
    // of sub-exceptions...
    // TODO - it would be nice to see this moved out of tests to the main
    // source so that it can be used as a convenience method by users also...
    private void processValidationFailures(final ValidationException ve) {
        List<ValidationException> causes = ve.getCausingExceptions();
        if (causes.isEmpty()) {
            // This was a leaf node, i.e. only one validation failure
            validationFailureList.add(ve.getMessage());
        } else {
            // Multiple validation failures exist, so process the sub-exceptions
            // to obtain them. NOTE: Not sure we should keep the message from
            // the current exception in this case. When there are causing
            // exceptions, the message in the containing exception is merely
            // summary information, e.g. "2 schema violations found".
            validationFailureList.add(ve.getMessage());
            causes.forEach(this::processValidationFailures);
        }
    }

    // Recursively process the expected values, which can contain nested arrays
    private void readExpectedValues(final JSONObject expected) {
        expectedFailureList.add((String) expected.get("message"));
        if (expected.has("causingExceptions")) {
            JSONArray causingEx = expected.getJSONArray("causingExceptions");
            for (Object subJson : causingEx) {
                readExpectedValues((JSONObject) subJson);
            }
        }
    }

}
