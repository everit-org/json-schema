package org.everit.json.schema.loader;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.TrueSchema;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.everit.json.schema.TestSupport.loadAsV6;
import static org.everit.json.schema.TestSupport.v6Loader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertTrue(SchemaLoader.load(get("additionalItemSchema")) instanceof ArraySchema);
    }

    @Test
    public void arrayByAdditionalItems() {
        ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arrayByAdditionalItems"));
        Assert.assertFalse(actual.requiresArray());
    }

    @Test
    public void arrayByItems() {
        ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arrayByItems"));
        assertNotNull(actual);
    }

    @Test
    public void arraySchema() {
        ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arraySchema"));
        assertNotNull(actual);
        assertEquals(2, actual.getMinItems().intValue());
        assertEquals(3, actual.getMaxItems().intValue());
        assertTrue(actual.needsUniqueItems());
        assertEquals(NullSchema.INSTANCE, actual.getAllItemSchema());
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

    @Test
    public void v6LoaderSupportsContains() {
        ArraySchema result = (ArraySchema) loadAsV6(get("arrayWithContains"));
        assertNotNull(result.getContainedItemSchema());
    }

    @Test
    public void v4LoaderDoesNotSupportContains() {
        ArraySchema result = (ArraySchema) SchemaLoader.load(get("arrayWithContains"));
        assertNull(result.getContainedItemSchema());
    }

    @Test
    public void itemsCanBeBooleanInV6() {
        ArraySchema actual = (ArraySchema) loadAsV6(get("itemsAsBoolean"));
        assertEquals(TrueSchema.builder().build(), actual.getAllItemSchema());
    }

}
