package org.everit.json.schema.loader;

import static java8.util.Objects.requireNonNull;
import java8.util.stream.StreamSupport;
import static java8.util.stream.Collectors.toList;
import static java8.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java8.util.function.Function;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.Schema;

/**
 * @author erosb
 */
class CombinedSchemaLoader implements SchemaExtractor {

    /**
     * Alias for {@code Function<Collection<Schema>, CombinedSchema.Builder>}.
     */
    @FunctionalInterface
    private interface CombinedSchemaProvider
            extends Function<Collection<Schema>, CombinedSchema.Builder> {

    }

    private static final Map<String, CombinedSchemaProvider> COMB_SCHEMA_PROVIDERS = new HashMap<>(3);

    static {
        COMB_SCHEMA_PROVIDERS.put("allOf", CombinedSchema::allOf);
        COMB_SCHEMA_PROVIDERS.put("anyOf", CombinedSchema::anyOf);
        COMB_SCHEMA_PROVIDERS.put("oneOf", CombinedSchema::oneOf);
    }

    private final SchemaLoader defaultLoader;

    public CombinedSchemaLoader(SchemaLoader defaultLoader) {
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    @Override
    public ExtractionResult extract(JsonObject schemaJson) {
        Set<String> presentKeys = StreamSupport.stream(COMB_SCHEMA_PROVIDERS.keySet())
                .filter(schemaJson::containsKey)
                .collect(toSet());
        Collection<Schema.Builder<?>> extractedSchemas = StreamSupport.stream(presentKeys).map(key -> loadCombinedSchemaForKeyword(schemaJson, key))
                .collect(toList());
        return new ExtractionResult(presentKeys, extractedSchemas);
    }

    private CombinedSchema.Builder loadCombinedSchemaForKeyword(JsonObject schemaJson, String key) {
        Collection<Schema> subschemas = new ArrayList<>();
        schemaJson.require(key).requireArray()
                .forEach((i, subschema) -> subschemas.add(defaultLoader.loadChild(subschema).build()));
        return COMB_SCHEMA_PROVIDERS.get(key).apply(subschemas);
    }

}
