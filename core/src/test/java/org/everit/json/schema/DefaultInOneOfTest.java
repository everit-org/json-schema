package org.everit.json.schema;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.everit.json.schema.TestSupport.loadAsV6;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultInOneOfTest {

    private static final ResourceLoader LOADER = ResourceLoader.DEFAULT;

    @Test
    public void defaultInFirstOneOf() {
        JSONObject jsonSchema = LOADER.readObj("default-in-first-oneof-schema.json");
        JSONObject jsonSpecification = LOADER.readObj("default-in-oneof.json");
        Schema schema = SchemaLoader
                .builder()
                .useDefaults(true)
                .schemaJson(jsonSchema)
                .build()
                .load()
                .build();
        assertDoesNotThrow(() -> schema.validate(jsonSpecification));
        assertTrue(jsonSpecification.getJSONObject("parameter1").has("allowEmptyValue"));
        assertFalse(jsonSpecification.getJSONObject("parameter1").getBoolean("allowEmptyValue"));
        assertFalse(jsonSpecification.getJSONObject("parameter2").has("allowEmptyValue"));
    }

    @Test
    public void defaultInLastOneOf() {
        JSONObject jsonSchema = LOADER.readObj("default-in-last-oneof-schema.json");
        JSONObject jsonSpecification = LOADER.readObj("default-in-oneof.json");
        Schema schema = SchemaLoader
                .builder()
                .useDefaults(true)
                .schemaJson(jsonSchema)
                .build()
                .load()
                .build();
        assertDoesNotThrow(() -> schema.validate(jsonSpecification));
        assertTrue(jsonSpecification.getJSONObject("parameter1").has("allowEmptyValue"));
        assertFalse(jsonSpecification.getJSONObject("parameter1").getBoolean("allowEmptyValue"));
        assertFalse(jsonSpecification.getJSONObject("parameter2").has("allowEmptyValue"));
    }

}
