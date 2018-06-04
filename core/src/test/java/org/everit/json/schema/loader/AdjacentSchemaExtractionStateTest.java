package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

import org.everit.json.schema.ConstSchema;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class AdjacentSchemaExtractionStateTest {

    @Test
    public void testReduce() {
        AdjacentSchemaExtractionState original = new AdjacentSchemaExtractionState(JsonValue.of(ImmutableMap.builder()
                .put("const", "2")
                .put("minimum", 1)
                .build()
        ).requireObject());
        ConstSchema.ConstSchemaBuilder schemaBuilder = ConstSchema.builder().permittedValue("2");

        AdjacentSchemaExtractionState actual = original.reduce(new ExtractionResult("const", asList(schemaBuilder)));

        assertEquals(singleton(schemaBuilder), actual.extractedSchemaBuilders());
        assertEquals(singleton("minimum"), actual.projectedSchemaJson().keySet());
    }
}
