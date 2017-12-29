package org.everit.json.schema;

public class EarlyFailingFailureReporter implements ValidationFailureReporter {

    @Override public void failure(String message, String keyword) {

    }

    @Override public void failure(Class<?> expectedType, Object actualValue) {

    }

    @Override public void failure(ValidationException exc) {

    }

    @Override public ValidationException inContextOfSchema(Schema schema, Runnable task) {
        return null;
    }

    @Override public void validationFinished() {

    }
}
