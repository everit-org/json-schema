package org.everit.json.schema;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectSchemaPropertyOrderTest
{
    @Test
    public void validatePropertyOrder()
    {
        String[] keys = IntStream
            .range(0, 100)
            .mapToObj(o -> UUID.randomUUID().toString())
            .toArray(String[]::new);
        ObjectSchema.Builder builder = ObjectSchema.builder();

        // add properties
        Arrays.stream(keys).forEach(k -> builder.addPropertySchema(k, new NumberSchema()));

        // make schema, get property keys
        ObjectSchema schema = builder.build();
        String[] schemaKeys = schema.getPropertySchemas().keySet().toArray(new String[]{});

        // validate key order
        for (int i = 0; i < keys.length; i++)
        {
            assertEquals(keys[i], schemaKeys[i]);
        }
    }
}
