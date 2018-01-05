package org.everit.json.schema;

public class EarlyFailingFailureReporter extends ValidationFailureReporter {

    public EarlyFailingFailureReporter(Schema schema) {
        super(schema);
    }

    @Override public void failure(ValidationException exc) {
        throw exc;
    }

    @Override public void validationFinished() {

    }
}
