package org.everit.json.schema;

class EarlyFailingFailureReporter extends ValidationFailureReporter {

    public EarlyFailingFailureReporter(Schema schema) {
        super(schema);
    }

    @Override public void failure(ValidationException exc) {
        throw exc;
    }

    @Override public void validationFinished() {

    }

    @Override ValidationException inContextOfSchema(Schema schema, Runnable task) {
        try {
            return super.inContextOfSchema(schema, task);
        } catch (ValidationException e) {
            return e;
        }
    }
}
