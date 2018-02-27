package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;

import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;

/**
 * @author erosb
 */
class ObjectSchemaLoader {

    private final LoadingState ls;

    private final LoaderConfig config;

    private final SchemaLoader defaultLoader;

    public ObjectSchemaLoader(LoadingState ls, LoaderConfig config, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.config = requireNonNull(config, "config cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    ObjectSchema.Builder load() {
        ObjectSchema.Builder builder = ObjectSchema.builder();
        ls.schemaJson().maybe("minProperties").map(JsonValue::requireInteger).ifPresent(builder::minProperties);
        ls.schemaJson().maybe("maxProperties").map(JsonValue::requireInteger).ifPresent(builder::maxProperties);
        ls.schemaJson().maybe("properties").map(JsonValue::requireObject)
                .ifPresent(propertyDefs -> populatePropertySchemas(propertyDefs, builder));
        ls.schemaJson().maybe("additionalProperties").ifPresent(rawAddProps -> {
            rawAddProps.canBe(Boolean.class, p -> builder.additionalProperties(p))
                    .or(JsonObject.class, def -> builder.schemaOfAdditionalProperties(defaultLoader.loadChild(def).build()))
                    .requireAny();
        });
        ls.schemaJson().maybe("required").map(JsonValue::requireArray)
                .ifPresent(arr -> arr.forEach((i, val) -> builder.addRequiredProperty(val.requireString())));
        ls.schemaJson().maybe("patternProperties").map(JsonValue::requireObject)
                .ifPresent(patternProps -> {
                    patternProps.keySet().forEach(pattern -> {
                        Schema patternSchema = defaultLoader.loadChild(patternProps.require(pattern)).build();
                        builder.patternProperty(pattern, patternSchema);
                    });
                });
        ls.schemaJson().maybe("dependencies").map(JsonValue::requireObject)
                .ifPresent(deps -> addDependencies(builder, deps));
        if (ls.specVersion().isAtLeast(DRAFT_6)) {
            ls.schemaJson().maybe("propertyNames")
                    .map(defaultLoader::loadChild)
                    .map(Schema.Builder::build)
                    .ifPresent(builder::propertyNameSchema);
        }
        return builder;
    }

    private void populatePropertySchemas(JsonObject propertyDefs,
            ObjectSchema.Builder builder) {
        propertyDefs.forEach((key, value) -> {
            if (!key.equals(ls.specVersion().idKeyword())
                    || value instanceof JsonObject) {
                addPropertySchemaDefinition(key, value, builder);
            }
        });
    }

    private void addPropertySchemaDefinition(String keyOfObj, JsonValue definition, ObjectSchema.Builder builder) {
        builder.addPropertySchema(keyOfObj, defaultLoader.loadChild(definition).build());
    }

    private void addDependencies(ObjectSchema.Builder builder, JsonObject deps) {
        deps.forEach((ifPresent, mustBePresent) -> addDependency(builder, ifPresent, mustBePresent));
    }

    private void addDependency(ObjectSchema.Builder builder, String ifPresent, JsonValue deps) {
        deps.canBeSchema(obj -> builder.schemaDependency(ifPresent, defaultLoader.loadChild(obj).build()))
                .or(JsonArray.class, arr -> arr.forEach((i, entry) -> builder.propertyDependency(ifPresent, entry.requireString())))
                .requireAny();
    }

}
