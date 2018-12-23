package org.everit.json.schema;

import java.util.function.BiFunction;

import org.everit.json.schema.event.ValidationListener;

public interface Validator {

    class ValidatorBuilder {

        private boolean failEarly = false;

        private ReadWriteContext readWriteContext;

        private ValidationListener validationListener = ValidationListener.NOOP;

        public ValidatorBuilder failEarly() {
            this.failEarly = true;
            return this;
        }

        public ValidatorBuilder readWriteContext(ReadWriteContext readWriteContext) {
            this.readWriteContext = readWriteContext;
            return this;
        }

        public ValidatorBuilder withListener(ValidationListener validationListener) {
            this.validationListener = validationListener;
            return this;
        }

        public Validator build() {
            return new DefaultValidator(failEarly, readWriteContext, validationListener);
        }

    }

    static ValidatorBuilder builder() {
        return new ValidatorBuilder();
    }

    void performValidation(Schema schema, Object input);
}

class DefaultValidator implements Validator {

    private BiFunction<Schema, Object, ValidatingVisitor> visitorFactory;

    private boolean failEarly;

    private final ReadWriteContext readWriteContext;

    private final ValidationListener validationListener;

    DefaultValidator(boolean failEarly, ReadWriteContext readWriteContext) {
        this(failEarly, readWriteContext, null);
    }

    DefaultValidator(boolean failEarly, ReadWriteContext readWriteContext, ValidationListener validationListener) {
        this.failEarly = failEarly;
        this.readWriteContext = readWriteContext;
        this.validationListener = validationListener;
    }

    @Override public void performValidation(Schema schema, Object input) {
        ValidationFailureReporter failureReporter = createFailureReporter(schema);
        ReadWriteValidator readWriteValidator = ReadWriteValidator.createForContext(readWriteContext, failureReporter);
        ValidatingVisitor visitor = new ValidatingVisitor(input, failureReporter, readWriteValidator, validationListener);
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
