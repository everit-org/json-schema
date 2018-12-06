package org.everit.json.schema.listener;

import org.everit.json.schema.ConditionalSchema;

public class ConditionalSchemaValidationEvent extends ValidationEvent<ConditionalSchema> {

    public ConditionalSchemaValidationEvent(ConditionalSchema schema, Object instance) {
        super(schema, instance);
    }

}
