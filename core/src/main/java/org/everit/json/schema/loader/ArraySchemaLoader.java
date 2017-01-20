package org.everit.json.schema.loader;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class ArraySchemaLoader {

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public ArraySchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    ArraySchema.Builder load() {
        final ArraySchema.Builder builder = ArraySchema.builder();
        ls.ifPresent("minItems", Integer.class, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                builder.minItems(integer);
            }
        });
        ls.ifPresent("maxItems", Integer.class, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                builder.maxItems(integer);
            }
        });
        ls.ifPresent("uniqueItems", Boolean.class, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                builder.uniqueItems(aBoolean);
            }
        });
        if (ls.schemaJson.has("additionalItems")) {
            ls.typeMultiplexer("additionalItems", ls.schemaJson.get("additionalItems"))
                    .ifIs(Boolean.class)
                    .then(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) {
                            builder.additionalItems(aBoolean);
                        }
                    })
                    .ifObject()
                    .then(new Consumer<JSONObject>() {
                        @Override
                        public void accept(JSONObject jsonObj) {
                            builder.schemaOfAdditionalItems(defaultLoader.loadChild(jsonObj).build());
                        }
                    })
                    .requireAny();
        }
        if (ls.schemaJson.has("items")) {
            ls.typeMultiplexer("items", ls.schemaJson.get("items"))
                    .ifObject()
                    .then(new Consumer<JSONObject>() {
                        @Override
                        public void accept(JSONObject itemSchema) {
                            builder.allItemSchema(defaultLoader.loadChild(itemSchema).build());
                        }
                    })
                    .ifIs(JSONArray.class)
                    .then(new Consumer<JSONArray>() {
                        @Override
                        public void accept(JSONArray arr) {
                            buildTupleSchema(builder, arr);
                        }
                    })
                    .requireAny();
        }
        return builder;
    }

    private void buildTupleSchema(final ArraySchema.Builder builder, final JSONArray itemSchema) {
        for (int i = 0; i < itemSchema.length(); ++i) {
            ls.typeMultiplexer(itemSchema.get(i))
                    .ifObject()
                    .then(new Consumer<JSONObject>() {
                        @Override
                        public void accept(JSONObject schema) {
                            builder.addItemSchema(defaultLoader.loadChild(schema).build());
                        }
                    })
                    .requireAny();
        }
    }

}
