package org.everit.json.schema.listener;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ValidationException;

public class CombinedSchemaMismatchEvent extends CombinedSchemaValidationEvent implements MismatchEvent {

    private final ValidationException failure;

    protected CombinedSchemaMismatchEvent(CombinedSchema schema, Object instance, ValidationException failure) {
        super(schema, instance);
        this.failure = failure;
    }

    @Override public ValidationException getFailure() {
        return failure;
    }
}
