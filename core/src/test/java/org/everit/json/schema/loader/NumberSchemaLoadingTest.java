package org.everit.json.schema.loader;

import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.TestSupport;
import org.json.JSONObject;
import org.junit.Test;

import static org.everit.json.schema.TestSupport.loadAsV6;
import static org.junit.Assert.assertEquals;

public class NumberSchemaLoadingTest {

    private static final JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("numberschemas.json");

    private static JSONObject get(String testcaseName) {
        return ALL_SCHEMAS.getJSONObject(testcaseName);
    }

    @Test
    public void v4Attributes() {
        NumberSchema expected = NumberSchema.builder()
                .minimum(5)
                .maximum(10)
                .multipleOf(2)
                .exclusiveMinimum(true)
                .exclusiveMaximum(true)
                .build();
        NumberSchema actual = (NumberSchema) SchemaLoader.load(get("v4Attributes"));

        assertEquals(expected, actual);
    }

    @Test
    public void v6Attributes() {
        NumberSchema expected = NumberSchema.builder()
                .minimum(5)
                .maximum(10)
                .multipleOf(2)
                .exclusiveMinimum(5)
                .exclusiveMaximum(10)
                .build();

        NumberSchema actual = (NumberSchema) loadAsV6(get("v6Attributes"));

        assertEquals(expected, actual);
    }

    @Test
    public void v6DoubleLimits() {
        NumberSchema expected = NumberSchema.builder()
                .requiresNumber(true)
                .exclusiveMinimum(5.5)
                .exclusiveMaximum(10.1)
                .build();

        NumberSchema actual = (NumberSchema) loadAsV6(get("v6DoubleLimits"));

        assertEquals(expected, actual);
    }

    @Test
    public void exclusiveMaximumIntegTest() {
        NumberSchema subject =(NumberSchema) loadAsV6(get("onlyExMax"));
        TestSupport.failureOf(subject).input(3.5).expect();

    }

}
