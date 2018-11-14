package org.everit.json.schema;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class SchemaVisitorListenerTest {

    public class DummySchemaVisitorListener implements SchemaVisitorListener {

        private List<Schema> validSchemas = new ArrayList<>();
        private List<Schema> invalidSchemas = new ArrayList<>();

        @Override
        public void addValidSchema(Schema schema) {
            validSchemas.add(schema);
        }

        @Override
        public void addInvalidSchema(Schema schema) {
            invalidSchemas.add(schema);
        }

        void clear() {
            validSchemas.clear();
            invalidSchemas.clear();
        }

        @Override
        public String toString() {
            List<String> valid = validSchemas
                    .stream().map(s -> String.format("{\"location\": \"%s\", \"schema\": %s}", s.getSchemaLocation(), s))
                    .collect(Collectors.toList());

            List<String> invalid = invalidSchemas
                    .stream().map(s -> String.format("{\"location\": \"%s\", \"schema\": %s}", s.getSchemaLocation(), s))
                    .collect(Collectors.toList());

            return String.format("{\"valid\": %s, \"invalid\": %s}", valid, invalid);
        }
    }


    private JSONObject resource = ResourceLoader.DEFAULT.readObj("complex-combined-test-schemas.json");

    private DummySchemaVisitorListener schemaVisitorListener = new DummySchemaVisitorListener();

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
