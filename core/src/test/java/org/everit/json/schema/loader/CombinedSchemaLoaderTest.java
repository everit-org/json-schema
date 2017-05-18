package org.everit.json.schema.loader;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.StringSchema;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author erosb
 */
public class CombinedSchemaLoaderTest {

    private static JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("combinedtestschemas.json");

    private static JSONObject get(final String schemaName) {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }


    @Test
    public void combinedSchemaLoading() {
        CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("combinedSchema"));
        Assert.assertNotNull(actual);
    }

    @Test
    public void combinedSchemaWithBaseSchema() {
        CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("combinedSchemaWithBaseSchema"));
        assertEquals(1, actual.getSubschemas().stream()
                .filter(schema -> schema instanceof StringSchema).count());
        assertEquals(1, actual.getSubschemas().stream()
                .filter(schema -> schema instanceof CombinedSchema).count());
    }

    @Test
    public void combinedSchemaWithExplicitBaseSchema() {
        CombinedSchema actual = (CombinedSchema) SchemaLoader
                .load(get("combinedSchemaWithExplicitBaseSchema"));
        assertEquals(1, actual.getSubschemas().stream()
                .filter(schema -> schema instanceof StringSchema).count());
        assertEquals(1, actual.getSubschemas().stream()
                .filter(schema -> schema instanceof CombinedSchema).count());
    }

    @Test
    public void combinedSchemaWithMultipleBaseSchemas() {
        Schema actual = SchemaLoader.load(get("combinedSchemaWithMultipleBaseSchemas"));
        assertTrue(actual instanceof CombinedSchema);
    }

    @Test public void multipleKeywordsFailure() {
        try {
            SchemaLoader.load(get("multipleKeywordsFailure"));
        } catch (SchemaException e) {
            assertEquals("#/properties/wrapper/items", e.getSchemaLocation());
        }
    }

}
