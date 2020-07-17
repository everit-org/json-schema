package org.everit.json.schema;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class HashCodeRecursionTest
{
    @Test
    public void hashCodeShouldNotProduceStackoverflowOnCyclicSchema() throws IOException
    {
        JSONObject schemaJson;
        try (InputStream inStream = getClass().getResourceAsStream("/org/everit/jsonvalidator/cyclic.json")) {
            schemaJson = new JSONObject(new JSONTokener(inStream));
        }

        Schema schema = new SchemaLoader.SchemaLoaderBuilder()
            .schemaJson(schemaJson)
            .build()
            .load()
            .build();

        schema.hashCode();
    }
}
