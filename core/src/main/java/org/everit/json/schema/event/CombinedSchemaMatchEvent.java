package org.everit.json.schema.event;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.Schema;
import org.json.JSONObject;

public class CombinedSchemaMatchEvent extends CombinedSchemaValidationEvent {

    public CombinedSchemaMatchEvent(CombinedSchema schema, Schema subSchema,
            Object instance) {
        super(schema, subSchema, instance);
    }

    @Override void describeTo(JSONObject obj) {
        obj.put("type", "match");
        obj.put("keyword", schema.getCriterion().toString());
    }

    @Override public boolean equals(Object o) {
        return o instanceof CombinedSchemaMatchEvent && super.equals(o);
    }

    @Override boolean canEqual(Object o) {
        return o instanceof CombinedSchemaMatchEvent;
    }
}
