package org.everit.json.schema;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.OrgJsonUtil.toMap;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.regexp.RE2JRegexpFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

@RunWith(Parameterized.class)
public class IssueTest {

    @Parameters(name = "{1}")
    public static List<Object[]> params() {
        List<Object[]> rval = new ArrayList<>();
        Reflections refs = new Reflections("org.everit.json.schema.issues",
                new ResourcesScanner());
        Set<String> paths = refs.getResources(Pattern.compile("schema.json"))
                .stream().map(path -> path.substring(0, path.lastIndexOf('/')))
                .collect(Collectors.toSet());
        for (String path : paths) {
            rval.add(new Object[] { path, path.substring(path.lastIndexOf('/') + 1) });
        }
        return rval;
    }

    private final String issueDir;

    private final String testCaseName;

    private JettyWrapper servletSupport;

    private List<String> validationFailureList;

    private List<String> expectedFailureList;

    private SchemaLoader.SchemaLoaderBuilder loaderBuilder;

    private Validator.ValidatorBuilder validatorBuilder = Validator.builder();

    public IssueTest(String issueDir, String testCaseName) {
        this.issueDir = "/" + requireNonNull(issueDir, "issueDir cannot be null");
        this.testCaseName = testCaseName;
    }

    private Optional<InputStream> fileByName(final String fileName) {
        return Optional.ofNullable(getClass().getResourceAsStream(issueDir + "/" + fileName));
    }

    private void initJetty() {
        try {
            servletSupport = new JettyWrapper(issueDir + "/" + "remotes");
            servletSupport.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JSONObject streamAsJson(InputStream file) {
        try {
            return new JSONObject(new JSONTokener(IOUtils.toString(file)));
        } catch (java.io.IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Schema loadSchema() {
        Optional<InputStream> schemaFile = fileByName("schema.json");
        if (schemaFile.isPresent()) {
            JSONObject schemaObj = streamAsJson(schemaFile.get());
            loaderBuilder = SchemaLoader.builder().schemaJson(schemaObj);
            consumeValidatorConfig();
            return loaderBuilder.build().load().build();
        }
        throw new RuntimeException(issueDir + "/schema.json is not found");
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
            toMap(json).entrySet()
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
        fileByName("validator-config.json").map(file -> streamAsJson(file)).ifPresent(configJson -> {
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
        Assume.assumeFalse("issue dir starts with 'x' - ignoring", testCaseName.startsWith("x"));
        fileByName("remotes").ifPresent(unused -> initJetty());
        try {
            Schema schema = loadSchema();
            fileByName("subject-valid.json").ifPresent(file -> validate(file, schema, true));
            fileByName("subject-invalid.json").ifPresent(file -> validate(file, schema, false));
        } finally {
            stopJetty();
        }
    }

    private void validate(InputStream file, Schema schema, boolean shouldBeValid) {
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
            fail(failureBuilder.toString());
        }
        if (!shouldBeValid && thrown != null) {
            Optional<InputStream> expectedFile = fileByName("expectedException.json");
            if (expectedFile.isPresent()) {
                checkExpectedValues(expectedFile.get(), thrown);
            }
        }
        if (!shouldBeValid && thrown == null) {
            fail("did not throw ValidationException for invalid subject");
        }
    }

    // TODO - it would be nice to see this moved out of tests to the main
    // source so that it can be used as a convenience method by users also...
    private Object loadJsonFile(InputStream file) {

        Object subject = null;

        try {
            JSONTokener jsonTok = new JSONTokener(IOUtils.toString(file));

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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return subject;
    }

    private void sortCauses(JSONObject exc) {
        JSONArray causes = exc.optJSONArray("causingExceptions");
        if (causes != null) {
            List<JSONObject> causesList = new ArrayList<>(causes.length());
            for (int i = 0; i < causes.length(); ++i) {
                JSONObject item = causes.getJSONObject(i);
                sortCauses(item);
                causesList.add(item);
            }
            causesList.sort(Comparator.comparing(Object::toString));
            exc.put("causingExceptions", new JSONArray(causesList));
        }
    }

    private void checkExpectedValues(InputStream expectedExceptionsFile,
            ValidationException ve) {
        JSONObject expected = (JSONObject) loadJsonFile(expectedExceptionsFile);
        sortCauses(expected);
        JSONObject actual = ve.toJSON();
        sortCauses(actual);
        if (!ObjectComparator.deepEquals(actual, expected)) {
            fail("Expected: " + expected.toString(2) + "but was: " + actual.toString(2));
        }
    }

}
