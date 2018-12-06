package org.everit.json.schema.listener;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.Schema;

public class CombinedSchemaValidationEvent extends ValidationEvent<CombinedSchema> {

    private final Schema subSchema;

    public CombinedSchemaValidationEvent(CombinedSchema schema, Schema subSchema, Object instance) {
        super(schema, instance);
        this.subSchema = subSchema;
    }

    public Schema getSubSchema() {
        return subSchema;
    }
}
