package org.everit.json.schema;

import org.everit.json.schema.listener.AbstractSchemaEvent;
import org.everit.json.schema.listener.SubschemaMatchEvent;
import org.everit.json.schema.listener.SubschemaMismatchEvent;
import org.everit.json.schema.listener.SchemaReferencedEvent;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class SchemaVisitorListenerTest {

    public class DummySchemaVisitorListener implements SchemaVisitorListener {

        private List<SubschemaMatchEvent> validSchemas = new ArrayList<>();
        private List<SubschemaMismatchEvent> invalidSchemas = new ArrayList<>();
        private List<SchemaReferencedEvent> referencedSchemas = new ArrayList<>();

        @Override
        public void subschemaMatch(SubschemaMatchEvent matchEvent) {
            validSchemas.add(matchEvent);
        }

        @Override
        public void subschemaMismatch(SubschemaMismatchEvent mismatchEvent) {
            invalidSchemas.add(mismatchEvent);
        }

        @Override
        public void schemaReferenced(SchemaReferencedEvent referencedEvent) {
            referencedSchemas.add(referencedEvent);
        }

        void clear() {
            validSchemas.clear();
            invalidSchemas.clear();
            referencedSchemas.clear();
        }

        String eventToString(AbstractSchemaEvent event) {
            List<String> failureMessages = null;
            if (event.getValidationException() != null) {
                failureMessages = event.getValidationException().getAllMessages();
            }

            Schema schema = event.getSchema();
            return String.format("{\"location\": \"%s\"", schema.getSchemaLocation()) +
                    String.format(",\"schema\": %s", schema) +
                    String.format(",\"failures\": \"%s\"}", failureMessages);
        }


        @Override
        public String toString() {
            List<String> valid = validSchemas.stream().map(this::eventToString).collect(Collectors.toList());
            List<String> invalid = invalidSchemas.stream().map(this::eventToString).collect(Collectors.toList());
            List<String> referenced = referencedSchemas.stream().map(this::eventToString).collect(Collectors.toList());

            return String.format("{\"valid\": %s, \"invalid\": %s, \"referenced\": %s}", valid, invalid, referenced);
        }

    }


    private JSONObject resource = ResourceLoader.DEFAULT.readObj("complex-combined-test-schemas.json");

    private DummySchemaVisitorListener schemaVisitorListener = new DummySchemaVisitorListener();

    private Validator validator = Validator.builder()
            .withListener(schemaVisitorListener)
            .build();

    private Validator validatorEarlyFailure = Validator.builder()
            .failEarly()
            .withListener(schemaVisitorListener)
            .build();

    @Before
    public void cleanListener() {
        schemaVisitorListener.clear();
    }

    private void testCase(Validator validator, String schemaPath, String eventPath, String expectedPath) {
        JSONObject schemaContent = resource.getJSONObject(schemaPath);
        Schema schema = TestSupport.loadAsV7(schemaContent);

        JSONObject event = resource.getJSONObject(eventPath);
        try {
            validator.performValidation(schema, event);
        } catch (Exception ignored) {
        }

        JSONObject validation = new JSONObject(schemaVisitorListener.toString());
        JSONObject expectedValidations = resource.getJSONObject(expectedPath);
        assertEquals(validation.toString(), expectedValidations.toString());

        schemaVisitorListener.clear();
    }

    private void testCase(String schemaPath, String eventPath, String expectedPath) {
        testCase(validator, schemaPath, eventPath, expectedPath);
    }

    @Test
    public void refSchema() {
        testCase("refSchema1", "refExample1", "refExpected1");
    }

    @Test
    public void combinedSchemaOneOf() {
        testCase("combinedSchema2", "combinedExample2", "combinedExpected2");
    }

    @Test
    public void directRefSchema() {
        testCase("refSchema3", "refExample3", "refExpected3");
    }

    @Test
    public void ifThenElseSchema() {
        testCase("ifThenElseSchema", "ifThenElseExample4.1", "ifThenElseExpected4.1");
        testCase("ifThenElseSchema", "ifThenElseExample4.2", "ifThenElseExpected4.2");
        testCase("ifThenElseSchema", "ifThenElseExample4.3", "ifThenElseExpected4.3");
    }

    @Test
    public void testWithFailEarlyMode() {
        testCase(validatorEarlyFailure, "refSchema1", "refExample1", "refExpected1");
        testCase(validatorEarlyFailure, "combinedSchema2", "combinedExample2", "combinedExpected2FailEarly");
        testCase(validatorEarlyFailure, "refSchema3", "refExample3", "refExpected3");

        testCase(validatorEarlyFailure, "ifThenElseSchema", "ifThenElseExample4.1", "ifThenElseExpected4.1");
        testCase(validatorEarlyFailure, "ifThenElseSchema", "ifThenElseExample4.2", "ifThenElseExpected4.2FailEarly");
        testCase(validatorEarlyFailure, "ifThenElseSchema", "ifThenElseExample4.3", "ifThenElseExpected4.3");
    }

}
