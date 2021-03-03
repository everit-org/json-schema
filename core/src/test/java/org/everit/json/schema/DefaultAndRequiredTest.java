package org.everit.json.schema;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultAndRequiredTest {

    private static final ResourceLoader LOADER = ResourceLoader.DEFAULT;

    @Test
    public void defaultAndRequired() {
        // tests consistent validation when useDefaults
        // is enabled and schema contains dependency on property with
        // default value; see issue #404
        int expectedExceptions = 20;
        int actualExceptions = 0;

        for (int i = 0; i < expectedExceptions; i++) {
            JSONObject jsonSchema = LOADER.readObj("default-and-required-schema.json");
            JSONObject jsonSpecification = LOADER.readObj("default-and-required.json");
            Schema schema = SchemaLoader
                    .builder()
                    .useDefaults(true)
                    .schemaJson(jsonSchema)
                    .build()
                    .load()
                    .build();

            try {
                schema.validate(jsonSpecification);
            } catch (ValidationException e) {
                assertEquals(1, e.getCausingExceptions().size());
                assertTrue(e.getMessage().contains("only 1 subschema matches out of 2"));
                actualExceptions += 1;
            }
        }

        assertEquals(expectedExceptions, actualExceptions);
    }
}
