package org.everit.json.schema;

public class ReadWriteValidators {
    static ReadWriteValidator createForContext(ReadWriteContext context, ValidationFailureReporter failureReporter) {
        return context == null ? ReadWriteValidator.NONE :
                context == ReadWriteContext.READ ? new WriteOnlyValidator(failureReporter) :
                        new ReadOnlyValidator(failureReporter);
    }
}
