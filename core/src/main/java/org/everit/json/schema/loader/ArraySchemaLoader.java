package org.everit.json.schema.loader;

import org.everit.json.schema.ArraySchema;
import org.json.JSONArray;

/**
 * @author erosb
 */
class ArraySchemaLoader {

    private LoadingState ls;

    ArraySchema.Builder load() {
        ArraySchema.Builder builder = ArraySchema.builder();
        ls.ifPresent("minItems", Integer.class, builder::minItems);
        ls.ifPresent("maxItems", Integer.class, builder::maxItems);
        ls.ifPresent("uniqueItems", Boolean.class, builder::uniqueItems);
        if (ls.schemaJson.has("additionalItems")) {
            ls.typeMultiplexer("additionalItems", ls.schemaJson.get("additionalItems"))
                    .ifIs(Boolean.class).then(builder::additionalItems)
                    .ifObject().then(jsonObj -> builder.schemaOfAdditionalItems(loadChild(jsonObj).build()))
                    .requireAny();
        }
        if (ls.schemaJson.has("items")) {
            ls.typeMultiplexer("items", ls.schemaJson.get("items"))
                    .ifObject().then(itemSchema -> builder.allItemSchema(loadChild(itemSchema).build()))
                    .ifIs(JSONArray.class).then(arr -> buildTupleSchema(builder, arr))
                    .requireAny();
        }
        return builder;
    }

    private void buildTupleSchema(final ArraySchema.Builder builder, final JSONArray itemSchema) {
        for (int i = 0; i < itemSchema.length(); ++i) {
            ls.typeMultiplexer(itemSchema.get(i))
                    .ifObject().then(schema -> builder.addItemSchema(loadChild(schema).build()))
                    .requireAny();
        }
    }

}
