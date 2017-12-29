package org.everit.json.schema;

/**
 * Internal interface receiving validation failures. Implementations are supposed to throw or collect {@link ValidationException} instances.
 * <p>
 * The validation always happens in the context of some "current schema", tracked by implementations.  This {@link Schema} instance will
 * be the {@link ValidationException#getViolatedSchema() violated schema} of the {@code ValidationException}s created.
 * </p>
 */
interface ValidationFailureReporter {

    void failure(String message, String keyword);

    void failure(Class<?> expectedType, Object actualValue);

    void failure(ValidationException exc);

    ValidationException inContextOfSchema(Schema schema, Runnable task);

    void validationFinished();
}
