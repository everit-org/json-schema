package org.everit.json.schema;

import org.everit.json.schema.*;

import java.util.function.BiFunction;

class DefaultValidator implements Validator {

    private BiFunction<Schema, Object, ValidatingVisitor> visitorFactory;

    private boolean failEarly;

    private final ReadWriteContext readWriteContext;

    DefaultValidator(boolean failEarly, ReadWriteContext readWriteContext) {
        this.failEarly = failEarly;
        this.readWriteContext = readWriteContext;
    }

    @Override public void performValidation(Schema schema, Object input) {
        ValidationFailureReporter failureReporter = createFailureReporter(schema);
        ReadWriteValidator readWriteValidator = ReadWriteValidators.createForContext(readWriteContext, failureReporter);
        ValidatingVisitor visitor = new ValidatingVisitor(input, failureReporter, readWriteValidator);
        visitor.visit(schema);
        visitor.failIfErrorFound();
    }

    private ValidationFailureReporter createFailureReporter(Schema schema) {
        if (failEarly) {
            return new EarlyFailingFailureReporter(schema);
        }
        return new CollectingFailureReporter(schema);
    }
}
