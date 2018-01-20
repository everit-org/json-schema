package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;

import org.everit.json.schema.ArraySchema;

/**
 * @author erosb
 */
class ArraySchemaLoader {

    private final LoadingState ls;

    private final LoaderConfig config;

    private final SchemaLoader defaultLoader;

    /**
     * Creates an instance configured with with {@link SpecificationVersion#DRAFT_4 draft 4 settings} and
     * {@link SpecificationVersion#defaultFormatValidators()}  default v4 format validators}.
     *
     * @deprecated use {@link #ArraySchemaLoader(LoadingState, LoaderConfig, SchemaLoader)} instead.
     */
    @Deprecated
    public ArraySchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this(ls, LoaderConfig.defaultV4Config(), defaultLoader);
    }

    ArraySchemaLoader(LoadingState ls,
            LoaderConfig config,
            SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.config = requireNonNull(config, "config cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    ArraySchema.Builder load() {
        ArraySchema.Builder builder = ArraySchema.builder();
        ls.schemaJson().maybe("minItems").map(JsonValue::requireInteger).ifPresent(builder::minItems);
        ls.schemaJson().maybe("maxItems").map(JsonValue::requireInteger).ifPresent(builder::maxItems);
        ls.schemaJson().maybe("uniqueItems").map(JsonValue::requireBoolean).ifPresent(builder::uniqueItems);
        ls.schemaJson().maybe("additionalItems").ifPresent(maybe -> {
            maybe.canBe(Boolean.class, builder::additionalItems)
                    .or(JsonObject.class, obj -> builder.schemaOfAdditionalItems(defaultLoader.loadChild(obj).build()))
                    .requireAny();
        });
        ls.schemaJson().maybe("items").ifPresent(items -> {
            items.canBeSchema(itemSchema -> builder.allItemSchema(defaultLoader.loadChild(itemSchema).build()))
                    .or(JsonArray.class, arr -> buildTupleSchema(builder, arr))
                    .requireAny();
        });
        if (config.specVersion != DRAFT_4) {
            ls.schemaJson().maybe("contains").ifPresent(containedRawSchema -> addContainedSchema(builder, containedRawSchema));
        }
        return builder;
    }

    private void addContainedSchema(ArraySchema.Builder builder, JsonValue schemaJson) {
        builder.containsItemSchema(defaultLoader.loadChild(schemaJson).build());
    }

    private void buildTupleSchema(ArraySchema.Builder builder, JsonArray itemSchema) {
        itemSchema.forEach((i, subschema) -> {
            builder.addItemSchema(defaultLoader.loadChild(subschema).build());
        });
    }

}
