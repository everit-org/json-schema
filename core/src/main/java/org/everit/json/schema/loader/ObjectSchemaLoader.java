package org.everit.json.schema.loader;

import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.SchemaException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class ObjectSchemaLoader {

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public ObjectSchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }


    ObjectSchema.Builder load() {
        ObjectSchema.Builder builder = ObjectSchema.builder();
        ls.ifPresent("minProperties", Integer.class, builder::minProperties);
        ls.ifPresent("maxProperties", Integer.class, builder::maxProperties);
        if (ls.schemaJson.has("properties")) {
            ls.typeMultiplexer(ls.schemaJson.get("properties"))
                    .ifObject().then(propertyDefs -> {
                populatePropertySchemas(propertyDefs, builder);
            }).requireAny();
        }
        if (ls.schemaJson.has("additionalProperties")) {
            ls.typeMultiplexer("additionalProperties", ls.schemaJson.get("additionalProperties"))
                    .ifIs(Boolean.class).then(builder::additionalProperties)
                    .ifObject().then(def -> builder.schemaOfAdditionalProperties(defaultLoader.loadChild(def).build()))
                    .requireAny();
        }
        if (ls.schemaJson.has("required")) {
            JSONArray requiredJson = ls.schemaJson.getJSONArray("required");
            new JSONTraverser(requiredJson).accept(new JSONVisitor() {

                @Override void visitArray(List<JSONTraverser> value) {
                    value.forEach(val -> val.accept(new JSONVisitor() {
                        @Override public void visitString(String value) {
                            builder.addRequiredProperty(value);
                        }
                    }));
                }

            });
        }
        if (ls.schemaJson.has("patternProperties")) {
            JSONObject patternPropsJson = ls.schemaJson.getJSONObject("patternProperties");
            String[] patterns = JSONObject.getNames(patternPropsJson);
            if (patterns != null) {
                for (String pattern : patterns) {
                    builder.patternProperty(pattern, defaultLoader.loadChild(patternPropsJson.getJSONObject(pattern))
                            .build());
                }
            }
        }
        ls.ifPresent("dependencies", JSONObject.class, deps -> addDependencies(builder, deps));
        return builder;
    }

    private void populatePropertySchemas(JSONObject propertyDefs,
            ObjectSchema.Builder builder) {
        String[] names = JSONObject.getNames(propertyDefs);
        if (names == null || names.length == 0) {
            return;
        }
        Arrays.stream(names).forEach(key -> {
            addPropertySchemaDefinition(key, propertyDefs.get(key), builder);
        });
    }

    private void addPropertySchemaDefinition(final String keyOfObj, final Object definition,
            final ObjectSchema.Builder builder) {
        ls.typeMultiplexer(definition)
                .ifObject()
                .then(obj -> {
                    builder.addPropertySchema(keyOfObj, defaultLoader.loadChild(obj).build());
                })
                .requireAny();
    }

    private void addDependencies(final ObjectSchema.Builder builder, final JSONObject deps) {
        Arrays.stream(JSONObject.getNames(deps))
                .forEach(ifPresent -> addDependency(builder, ifPresent, deps.get(ifPresent)));
    }

    private void addDependency(final ObjectSchema.Builder builder, final String ifPresent, final Object deps) {
        ls.typeMultiplexer(deps)
                .ifObject().then(obj -> {
            builder.schemaDependency(ifPresent, defaultLoader.loadChild(obj).build());
        }).ifIs(JSONArray.class).then(propNames -> {
            IntStream.range(0, propNames.length())
                    .mapToObj(i -> propNames.getString(i))
                    .forEach(dependency -> builder.propertyDependency(ifPresent, dependency));
        }).requireAny();
    }



}
