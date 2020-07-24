package org.everit.json.schema;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class HashCodeRecursionTest
{
    @Test
    public void hashCodeShouldNotProduceStackoverflowOnCyclicSchema() throws IOException
    {
        loadSelfCyclic().hashCode();
    }

    @Test
    public void equalsShouldNotProduceStackoverflowOnCyclicSchema() throws IOException
    {
        CombinedSchema cyclic = (CombinedSchema) loadSelfCyclic();
        CombinedSchema cyclicCopy = (CombinedSchema) loadSelfCyclic();

        assertEquals(cyclic, cyclicCopy);
    }

    private Schema loadSelfCyclic() throws IOException
    {
        JSONObject schemaJson;
        try (InputStream inStream = getClass().getResourceAsStream("/org/everit/jsonvalidator/cyclic.json")) {
            schemaJson = new JSONObject(new JSONTokener(inStream));
        }

        return new SchemaLoader.SchemaLoaderBuilder()
            .schemaJson(schemaJson)
            .build()
            .load()
            .build();
    }
}
