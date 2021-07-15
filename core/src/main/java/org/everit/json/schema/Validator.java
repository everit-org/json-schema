package org.everit.json.schema;

import org.everit.json.schema.event.ValidationListener;

public interface Validator {

    class ValidatorBuilder {

        private boolean failEarly = false;

        private ReadWriteContext readWriteContext;

        private ValidationListener validationListener = ValidationListener.NOOP;

        private PrimitiveParsingPolicy primitiveParsingPolicy = PrimitiveParsingPolicy.STRICT;

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

        public ValidatorBuilder primitiveParsingPolicy(PrimitiveParsingPolicy primitiveParsingPolicy) {
            this.primitiveParsingPolicy = primitiveParsingPolicy;
            return this;
        }

        public Validator build() {
            return new DefaultValidator(failEarly, readWriteContext, validationListener, primitiveParsingPolicy);
        }
    }

    static ValidatorBuilder builder() {
        return new ValidatorBuilder();
    }

    void performValidation(Schema schema, Object input);
}

class DefaultValidator implements Validator {

    private boolean failEarly;

    private final ReadWriteContext readWriteContext;

    private final ValidationListener validationListener;

    private final PrimitiveParsingPolicy primitiveParsingPolicy;

    DefaultValidator(boolean failEarly, ReadWriteContext readWriteContext, ValidationListener validationListener,
                     PrimitiveParsingPolicy primitiveParsingPolicy) {
        this.failEarly = failEarly;
        this.readWriteContext = readWriteContext;
        this.validationListener = validationListener;
        this.primitiveParsingPolicy = primitiveParsingPolicy;
    }

    @Override public void performValidation(Schema schema, Object input) {
        ValidationFailureReporter failureReporter = createFailureReporter(schema);
        ReadWriteValidator readWriteValidator = ReadWriteValidator.createForContext(readWriteContext, failureReporter);
        ValidatingVisitor visitor = new ValidatingVisitor(input, failureReporter, readWriteValidator, validationListener,
                primitiveParsingPolicy);
        try {
            visitor.visit(schema);
            visitor.failIfErrorFound();
        } catch (InternalValidationException e) {
            throw e.copy();
        }
    }

    private ValidationFailureReporter createFailureReporter(Schema schema) {
        if (failEarly) {
            return new EarlyFailingFailureReporter(schema);
        }
        return new CollectingFailureReporter(schema);
    }
}
