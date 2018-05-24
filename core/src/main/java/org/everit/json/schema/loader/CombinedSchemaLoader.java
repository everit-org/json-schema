package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
    public Collection<Schema.Builder<?>> extract(LoadingState ls) {
        List<String> presentKeys = COMB_SCHEMA_PROVIDERS.keySet().stream()
                .filter(ls.schemaJson()::containsKey)
                .collect(toList());
        return presentKeys.stream().map(key -> loadCombinedSchemaForKeyword(ls, key)).collect(toList());
    }

    private CombinedSchema.Builder loadCombinedSchemaForKeyword(LoadingState ls, String key) {
        Collection<Schema> subschemas = new ArrayList<>();
        ls.schemaJson().require(key).requireArray()
                .forEach((i, subschema) -> subschemas.add(defaultLoader.loadChild(subschema).build()));
        return COMB_SCHEMA_PROVIDERS.get(key).apply(subschemas);
    }

}
