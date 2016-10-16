package org.everit.json.schema.loader;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.json.JSONArray;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class CombinedSchemaLoader {

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

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public CombinedSchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    public Optional<Schema.Builder<?>> load() {
        List<String> presentKeys = COMB_SCHEMA_PROVIDERS.keySet().stream()
                .filter(ls.schemaJson::has)
                .collect(Collectors.toList());
        if (presentKeys.size() > 1) {
            throw new SchemaException(String.format(
                    "expected at most 1 of 'allOf', 'anyOf', 'oneOf', %d found", presentKeys.size()));
        } else if (presentKeys.size() == 1) {
            String key = presentKeys.get(0);
            JSONArray subschemaDefs = ls.schemaJson.getJSONArray(key);
            Collection<Schema> subschemas = IntStream.range(0, subschemaDefs.length())
                    .mapToObj(subschemaDefs::getJSONObject)
                    .map(defaultLoader::loadChild)
                    .map(Schema.Builder::build)
                    .collect(Collectors.toList());
            CombinedSchema.Builder combinedSchema = COMB_SCHEMA_PROVIDERS.get(key).apply(
                    subschemas);
            Schema.Builder<?> baseSchema;
            if (ls.schemaJson.has("type")) {
                baseSchema = defaultLoader.loadForType(ls.schemaJson.get("type"));
            } else {
                baseSchema = defaultLoader.sniffSchemaByProps();
            }
            if (baseSchema == null) {
                return Optional.of(combinedSchema);
            } else {
                return Optional.of(CombinedSchema.allOf(asList(baseSchema.build(),
                        combinedSchema.build())));
            }
        } else {
            return Optional.empty();
        }
    }

}
