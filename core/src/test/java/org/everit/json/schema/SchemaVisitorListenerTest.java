package org.everit.json.schema;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SchemaVisitorListenerTest {

    private JSONObject resource = ResourceLoader.DEFAULT.readObj("complex-combined-test-schemas.json");

    private SchemaVisitorListener schemaVisitorListener = new SchemaVisitorListener();

    private Validator validator = Validator.builder()
            .withListener(schemaVisitorListener)
            .build();

    @Before
    public void cleanListener() {
        schemaVisitorListener.clear();
    }

    private void testCase(String schemaPath, String eventPath, String expectedPath) {
        JSONObject schemaContent = resource.getJSONObject(schemaPath);
        Schema schema = TestSupport.loadAsV7(schemaContent);

        JSONObject event = resource.getJSONObject(eventPath);
        validator.performValidation(schema, event);

        JSONObject expectedValidations = resource.getJSONObject(expectedPath);
        JSONObject validation = new JSONObject(schemaVisitorListener.toString());

        assertEquals(validation.toString(), expectedValidations.toString());
    }

    @Test
    public void refSchema() {
        testCase("schema1", "example1", "expected1");
    }

    @Test
    public void combinedSchemaOneOf() {
        testCase("schema2", "example2", "expected2");
    }

}
