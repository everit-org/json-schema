package org.everit.json.schema.loader;

import org.everit.json.schema.ConditionalSchema;
import org.everit.json.schema.ResourceLoader;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.everit.json.schema.TestSupport.loadAsV7;


public class ConditionalSchemaLoaderTest {

    private static JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("conditionaltestschemas.json");

    private static JSONObject get(final String schemaName) {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    @Test
    public void conditionalSchemaLoading() {
        ConditionalSchema actual = (ConditionalSchema) loadAsV7(get("conditionalSchema"));
        Assert.assertNotNull(actual);
    }

}
