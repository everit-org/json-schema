package org.everit.json.schema;

import java.util.function.BiFunction;

public interface Validator {

    class ValidatorBuilder {

        private boolean failEarly = false;

        private ReadWriteContext readWriteContext;

        public ValidatorBuilder failEarly() {
            this.failEarly = true;
            return this;
        }

        public ValidatorBuilder readWriteContext(ReadWriteContext readWriteContext) {
            this.readWriteContext = readWriteContext;
            return this;
        }

        public Validator build() {
            return new DefaultValidator(failEarly, readWriteContext);
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

    DefaultValidator(boolean failEarly, ReadWriteContext readWriteContext) {
        this.failEarly = failEarly;
        this.readWriteContext = readWriteContext;
    }

    @Override public void performValidation(Schema schema, Object input) {
        ValidationFailureReporter failureReporter = createFailureReporter(schema);
        ReadWriteValidator readWriteValidator = ReadWriteValidator.createForContext(readWriteContext, failureReporter);
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
