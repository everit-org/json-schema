package org.everit.json.schema;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

/**
 * @author erosb
 */
public class TestCase {

    private static JSONArray loadTests(final InputStream input) {
        return new JSONArray(new JSONTokener(input));
    }

    static List<Object[]> loadAsParamsFromPackage(String packageName) {
        List<Object[]> rval = new ArrayList<>();
        Reflections refs = new Reflections(packageName,
                new ResourcesScanner());
        Set<String> paths = refs.getResources(Pattern.compile(".*\\.json"));
        for (String path : paths) {
            if (path.indexOf("/optional/") > -1 || path.indexOf("/remotes/") > -1) {
                continue;
            }
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            JSONArray arr = loadTests(TestSuiteTest.class.getResourceAsStream("/" + path));
            for (int i = 0; i < arr.length(); ++i) {
                JSONObject schemaTest = arr.getJSONObject(i);
                JSONArray testcaseInputs = schemaTest.getJSONArray("tests");
                for (int j = 0; j < testcaseInputs.length(); ++j) {
                    JSONObject input = testcaseInputs.getJSONObject(j);
                    TestCase testcase = new TestCase(input, schemaTest, fileName);
                    rval.add(new Object[] { testcase, testcase.schemaDescription });
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
        }
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

}
