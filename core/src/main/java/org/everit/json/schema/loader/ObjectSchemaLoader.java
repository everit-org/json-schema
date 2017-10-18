package org.everit.json.schema.loader;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;

/**
 * @author erosb
 */
class ObjectSchemaLoader {

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;
    private Map<String, FormatValidator> formatValidators;

    public ObjectSchemaLoader(LoadingState ls, SchemaLoader defaultLoader, Map<String, FormatValidator> formatValidators) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
        this.formatValidators = unmodifiableMap(requireNonNull(formatValidators, "formatValidators cannot be null"));
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
        if (DRAFT_6.equals(ls.specVersion())) {
            ls.schemaJson().maybe("propertyNames")
                    .map(defaultLoader::loadChild)
                    .map(Schema.Builder::build)
                    .ifPresent(builder::propertyNameSchema);
            ls.schemaJson().maybe("format").map(JsonValue::requireString).ifPresent(format -> addFormatValidator(builder, format));
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

    private void addFormatValidator(ObjectSchema.Builder builder, String formatName) {
        FormatValidator formatValidator = formatValidators.get(formatName);
        if (formatValidator != null) {
            builder.formatValidator(formatValidator);
        }
    }

}
