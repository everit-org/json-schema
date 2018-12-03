package org.everit.json.schema.listener;

import org.everit.json.schema.ConditionalSchema;
import org.everit.json.schema.ValidationException;

public class ConditionalSchemaMismatchEvent extends ConditionalSchemaValidationEvent implements MismatchEvent {

    private final ValidationException failure;

    protected ConditionalSchemaMismatchEvent(ConditionalSchema schema, Object instance, ValidationException failure) {
        super(schema, instance);
        this.failure = failure;
    }

    @Override public ValidationException getFailure() {
        return failure;
    }
}
