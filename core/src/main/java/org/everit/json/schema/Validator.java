package org.everit.json.schema;

import java.util.function.BiFunction;

public interface Validator {

    class ValidatorBuilder {

        private boolean failEarly = false;

        public ValidatorBuilder failEarly() {
            this.failEarly = true;
            return this;
        }

        public Validator build() {
            return new DefaultValidator(failEarly);
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

    public DefaultValidator(boolean failEarly) {
        this.failEarly = failEarly;
    }

    @Override public void performValidation(Schema schema, Object input) {
        ValidationFailureReporter failureReporter = createFailureReporter(schema);
        ValidatingVisitor visitor = new ValidatingVisitor(input, failureReporter);
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
