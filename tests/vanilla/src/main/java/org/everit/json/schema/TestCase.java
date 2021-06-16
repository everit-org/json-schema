package org.everit.json.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.params.provider.Arguments;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author erosb
 */
public class TestCase {

    private static JSONArray loadTests(InputStream input) {
        try {
            return new JSONArray(new JSONTokener(IOUtils.toString(new InputStreamReader(input))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<Arguments> loadAsParamsFromPackage(String packageName) {
        return loadAsParamsFromPackage(packageName, emptyList());
    }

    static List<Arguments> loadAsParamsFromPackage(String packageName, Collection<String> excludePatterns) {
        List<Arguments> rval = new ArrayList<>();
        Reflections refs = new Reflections(packageName,
                new ResourcesScanner());
        Set<String> paths = refs.getResources(Pattern.compile(".*\\.json"));
        for (String path : paths) {
            if (path.indexOf("/optional/") > -1 || path.indexOf("/remotes/") > -1) {
                continue;
            }
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            if (excludePatterns.stream().anyMatch(fileName::contains)) {
                continue;
            }
            JSONArray arr = loadTests(V4TestSuiteTest.class.getResourceAsStream("/" + path));
            for (int i = 0; i < arr.length(); ++i) {
                JSONObject schemaTest = arr.getJSONObject(i);
                JSONArray testcaseInputs = schemaTest.getJSONArray("tests");
                for (int j = 0; j < testcaseInputs.length(); ++j) {
                    JSONObject input = testcaseInputs.getJSONObject(j);
                    TestCase testcase = new TestCase(input, schemaTest, fileName);
                    rval.add(Arguments.of(testcase, testcase.schemaDescription ));
                }
            }
        }
        return rval;
    }

    final String schemaDescription;

    final Object schemaJson;

    final String inputDescription;

    final Object inputData;

    final boolean expectedToBeValid;

    private Schema schema;

    private TestCase(JSONObject input, JSONObject schemaTest, String fileName) {
        schemaDescription = "[" + fileName + "]/" + schemaTest.getString("description");
        schemaJson = schemaTest.get("schema");
        inputDescription = "[" + fileName + "]/" + input.getString("description");
        expectedToBeValid = input.getBoolean("valid");
        inputData = input.get("data");
    }

    public void runTestInEarlyFailureMode() {
        testWithValidator(Validator.builder().failEarly().build(), schema);
    }

    private void testWithValidator(Validator validator, Schema schema) {
        try {
            validator.performValidation(schema, inputData);
            if (!expectedToBeValid) {
                throw new AssertionError("false success for " + inputDescription);
            }
        } catch (ValidationException e) {
            if (expectedToBeValid) {
                throw new AssertionError("false failure for " + inputDescription, e);
            }
            verifyStacktraces(e);
        }
    }

    private static void verifyStacktraces(ValidationException e) {
        assertNotEquals(0, e.getStackTrace().length);
        assertEmptyCauseStackTraces(e).ifPresent(nonempty -> {
            throw new AssertionError("non-empty stacktrace: " + nonempty);
        });
    }

    private static Optional<ValidationException> assertEmptyCauseStackTraces(ValidationException e) {
        return e.getCausingExceptions().stream().filter(exc -> exc.getStackTrace().length > 0)
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> e.getCausingExceptions().stream()
                        .map(TestCase::assertEmptyCauseStackTraces)
                        .filter(Optional::isPresent)
                        .findFirst()
                        .orElse(Optional.empty()));
    }

    public void loadSchema(SchemaLoader.SchemaLoaderBuilder loaderBuilder) {
        try {
            SchemaLoader loader = loaderBuilder.schemaJson(schemaJson).build();
            this.schema = loader.load().build();
        } catch (SchemaException e) {
            throw new AssertionError("schema loading failure for " + schemaDescription, e);
        } catch (JSONException e) {
            throw new AssertionError("schema loading error for " + schemaDescription, e);
        }
    }

    public void runTestInCollectingMode() {
        testWithValidator(Validator.builder().build(), schema);
    }

    @Override
    public String toString() {
        return schemaDescription + "/" + inputDescription;
    }
}
