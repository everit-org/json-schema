package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaLocation;
import org.everit.json.schema.StringSchema;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

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
        assertNotNull(actual);
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

    @Test
    public void multipleCombinedSchemasAtTheSameNestingLevel() {
        SchemaLoader defaultLoader = SchemaLoader.builder().schemaJson(get("multipleKeywords")).build();
        JsonObject json = JsonValue.of(get("multipleKeywords")).requireObject();
        new LoadingState(LoaderConfig.defaultV4Config(), emptyMap(), json, json, null, SchemaLocation.empty());
        CombinedSchemaLoader subject = new CombinedSchemaLoader(defaultLoader);
        Set<Schema> actual = new HashSet<>(
                subject.extract(json).extractedSchemas.stream().map(builder -> builder.build()).collect(toList()));
        HashSet<CombinedSchema> expected = new HashSet<>(asList(
                CombinedSchema.allOf(singletonList(BooleanSchema.INSTANCE)).build(),
                CombinedSchema.anyOf(singletonList(StringSchema.builder().build())).build()
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void loadTheSameCombinedSeveralTimes() {
        JSONObject json = new JSONObject(new JSONTokener("{\"enum\": [\"V1\", \"V2\", \"V3\"],\"type\": \"string\"}"));

        for (int i = 0; i < 100000; ++i) {
            Schema s0 = SchemaLoader.load(json);
            Schema s1 = SchemaLoader.load(json);

            System.out.println("Iter: " + i + ", equals=" + Objects.equals(s0, s1));

            if (i > 10) {
                assertEquals(s0, s1);
            }
        }
    }
}
