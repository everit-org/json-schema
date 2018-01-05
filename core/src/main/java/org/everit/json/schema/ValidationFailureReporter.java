package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

/**
 * Internal interface receiving validation failures. Implementations are supposed to throw or collect {@link ValidationException} instances.
 * <p>
 * The validation always happens in the context of some "current schema". This {@link Schema} instance will
 * be the {@link ValidationException#getViolatedSchema() violated schema} of the {@code ValidationException}s created.
 * </p>
 */
abstract class ValidationFailureReporter {

    protected Schema schema;

    ValidationFailureReporter(Schema schema) {
        this.schema = requireNonNull(schema, "schema cannot be null");
    }

    void failure(String message, String keyword) {
        failure(new ValidationException(schema, message, keyword, schema.getSchemaLocation()));
    }

    void failure(Class<?> expectedType, Object actualValue) {
        failure(new ValidationException(schema, expectedType, actualValue, "type", schema.getSchemaLocation()));
    }

    abstract void failure(ValidationException exc);

    ValidationException inContextOfSchema(Schema schema, Runnable task) {
        requireNonNull(schema, "schema cannot be null");
        Schema origSchema = this.schema;
        this.schema = schema;
        task.run();
        this.schema = origSchema;
        return null;
    }

    abstract void validationFinished();
}
