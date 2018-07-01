package org.everit.json.schema;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.everit.json.schema.loader.JsonArray;
import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.JsonValue;
import org.everit.json.schema.loader.SchemaLoader;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author erosb
 */
public class TestCase {

    private static ArrayNode loadTests(final InputStream input) {
    	ObjectMapper objectMapper = new ObjectMapper();
    	ArrayNode value;
		try {
			value = (ArrayNode)objectMapper.readTree(input);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
        return value;
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
            ArrayNode arr = loadTests(TestSuiteTest.class.getResourceAsStream("/" + path));
            for (int i = 0; i < arr.size(); ++i) {
            	JsonNode schemaTest = arr.get(i);
            	ArrayNode testcaseInputs = (ArrayNode)schemaTest.get("tests");
                for (int j = 0; j < testcaseInputs.size(); ++j) {
                    JsonNode input = testcaseInputs.get(j);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private TestCase(JsonNode input, JsonNode schemaTest, String fileName) {
        schemaDescription = "[" + fileName + "]/" + schemaTest.get("description");
        schemaJson = schemaTest.get("schema");
        inputDescription = "[" + fileName + "]/" + input.get("description");
        expectedToBeValid = input.get("valid").asBoolean();
        Object obj = JsonSchemaUtil.nodeToObject((JsonNode)input.get("data"));
        if(obj instanceof Map) {
        	inputData = new JsonObject((Map)obj);
        } else if (obj instanceof List) {
        	inputData = new JsonArray((List)obj);
        } else if (obj == null) {
        	inputData = new JsonObject(new HashMap());
        } else {
        	inputData = obj;
        }
        
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
            SchemaLoader loader = loaderBuilder.schemaJson(JsonValue.of(schemaJson)).build();
            this.schema = loader.load().build();
        } catch (SchemaException e) {
            throw new AssertionError("schema loading failure for " + schemaDescription, e);
        }
    }

    public void runTestInCollectingMode() {
        testWithValidator(Validator.builder().build(), schema);
    }

}
