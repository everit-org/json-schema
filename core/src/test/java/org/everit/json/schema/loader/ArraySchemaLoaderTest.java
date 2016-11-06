package org.everit.json.schema.loader;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.SchemaException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author erosb
 */
public class ArraySchemaLoaderTest {

    private static JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("arraytestschemas.json");

    private static JSONObject get(final String schemaName) {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    @Test
    public void additionalItemSchema() {
        Assert.assertTrue(SchemaLoader.load(get("additionalItemSchema")) instanceof ArraySchema);
    }

    @Test
    public void arrayByAdditionalItems() {
        ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arrayByAdditionalItems"));
        Assert.assertFalse(actual.requiresArray());
    }

    @Test
    public void arrayByItems() {
        ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arrayByItems"));
        Assert.assertNotNull(actual);
    }

    @Test
    public void arraySchema() {
        ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arraySchema"));
        Assert.assertNotNull(actual);
        Assert.assertEquals(2, actual.getMinItems().intValue());
        Assert.assertEquals(3, actual.getMaxItems().intValue());
        Assert.assertTrue(actual.needsUniqueItems());
        Assert.assertEquals(NullSchema.INSTANCE, actual.getAllItemSchema());
    }

    @Test(expected = SchemaException.class)
    public void invalidAdditionalItems() {
        SchemaLoader.load(get("invalidAdditionalItems"));
    }

    @Test(expected = SchemaException.class)
    public void invalidArrayItemSchema() {
        SchemaLoader.load(get("invalidArrayItemSchema"));
    }

    @Test(expected = SchemaException.class)
    public void invalidItemsArraySchema() {
        SchemaLoader.load(get("invalidItemsArraySchema"));
    }

}
