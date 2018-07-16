package org.everit.json.schema;

interface ReadWriteValidator {

    static ReadWriteValidator createForContext(ReadWriteContext context, ValidationFailureReporter failureReporter) {
        return context == null ? NONE :
                context == ReadWriteContext.READ ? new WriteOnlyValidator(failureReporter) :
                        new ReadOnlyValidator(failureReporter);
    }

    ReadWriteValidator NONE = (schema, subject) -> {
    };

    void validate(Schema schema, Object subject);

}

class ReadOnlyValidator implements ReadWriteValidator {

    private final ValidationFailureReporter failureReporter;

    ReadOnlyValidator(ValidationFailureReporter failureReporter) {
        this.failureReporter = failureReporter;
    }

    @Override public void validate(Schema schema, Object subject) {
        if (schema.isReadOnly() == Boolean.TRUE && subject != null) {
            failureReporter.failure("value is read-only", "readOnly");
        }
    }
}

class WriteOnlyValidator implements ReadWriteValidator {

    private final ValidationFailureReporter failureReporter;

    WriteOnlyValidator(ValidationFailureReporter failureReporter) {
        this.failureReporter = failureReporter;
    }

    @Override public void validate(Schema schema, Object subject) {
        if (schema.isWriteOnly() == Boolean.TRUE && subject != null) {
            failureReporter.failure("value is write-only", "writeOnly");
        }
    }
}
