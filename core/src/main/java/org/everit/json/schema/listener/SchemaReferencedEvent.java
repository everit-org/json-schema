package org.everit.json.schema.listener;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.json.JSONObject;

public class SchemaReferencedEvent extends ValidationEvent<ReferenceSchema> {

    private final Schema referredSchema;

    public SchemaReferencedEvent(ReferenceSchema schema, Object instance, Schema referredSchema) {
        super(schema, instance);
        this.referredSchema = referredSchema;
    }

    @Override
    void describeTo(JSONObject obj) {
        obj.put("type", "ref");
    }

    public Schema getReferredSchema() {
        return referredSchema;
    }
}
