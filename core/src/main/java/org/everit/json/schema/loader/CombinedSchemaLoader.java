package org.everit.json.schema.loader;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.CombinedSchema.allOf;

/**
 * @author erosb
 */
class CombinedSchemaLoader {

    /**
     * Alias for {@code Function<Collection<Schema>, CombinedSchema.Builder>}.
     */
    private interface CombinedSchemaProvider extends Function<Collection<Schema>, CombinedSchema.Builder> {
    }

    private static final Map<String, CombinedSchemaProvider> COMB_SCHEMA_PROVIDERS = new HashMap<>(3);

    static {
        COMB_SCHEMA_PROVIDERS.put("allOf", new CombinedSchemaProvider() {
            @Override
            public CombinedSchema.Builder apply(Collection<Schema> input) {
                return allOf(input);
            }
        });
        COMB_SCHEMA_PROVIDERS.put("anyOf", new CombinedSchemaProvider() {
            @Override
            public CombinedSchema.Builder apply(Collection<Schema> input) {
                return CombinedSchema.anyOf(input);
            }
        });
        COMB_SCHEMA_PROVIDERS.put("oneOf", new CombinedSchemaProvider() {
            @Override
            public CombinedSchema.Builder apply(Collection<Schema> input) {
                return CombinedSchema.oneOf(input);
            }
        });
    }

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public CombinedSchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    public Optional<? extends Schema.Builder<?>> load() {
        List<String> presentKeys = FluentIterable.from(COMB_SCHEMA_PROVIDERS.keySet())
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return ls.schemaJson.has(input);
                    }
                })
                .toList();
        if (presentKeys.size() > 1) {
            throw new SchemaException(String.format(
                    "expected at most 1 of 'allOf', 'anyOf', 'oneOf', %d found", presentKeys.size()));
        } else if (presentKeys.size() == 1) {
            String key = presentKeys.get(0);
            JSONArray subschemaDefs = ls.schemaJson.getJSONArray(key);

            List<Schema> subschemas = Lists.newArrayList();
            for (int i = 0; i < subschemaDefs.length(); i++) {
                JSONObject defObject = subschemaDefs.getJSONObject(i);
                subschemas.add(defaultLoader.loadChild(defObject).build());
            }

            CombinedSchema.Builder combinedSchema = COMB_SCHEMA_PROVIDERS.get(key).apply(subschemas);
            Schema.Builder<?> baseSchema;
            if (ls.schemaJson.has("type")) {
                baseSchema = defaultLoader.loadForType(ls.schemaJson.get("type"));
            } else {
                baseSchema = defaultLoader.sniffSchemaByProps();
            }
            if (baseSchema == null) {
                return Optional.of(combinedSchema);
            } else {
                return Optional.of(allOf(asList(baseSchema.build(), combinedSchema.build())));
            }
        } else {
            return Optional.absent();
        }
    }

}
