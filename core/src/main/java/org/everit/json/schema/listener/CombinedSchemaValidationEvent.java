package org.everit.json.schema.listener;

import org.everit.json.schema.CombinedSchema;

public class CombinedSchemaValidationEvent extends ValidationEvent<CombinedSchema> {

    protected CombinedSchemaValidationEvent(CombinedSchema schema, Object instance) {
        super(schema, instance);
    }

}
