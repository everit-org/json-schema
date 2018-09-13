package org.everit.json.schema;

import org.everit.json.schema.spi.JsonAdaptation;

import java.util.function.BiFunction;

public interface Validator {

    class ValidatorBuilder {

        private boolean failEarly = false;

        private ReadWriteContext readWriteContext;

        private JsonAdaptation jsonAdaptation = new JSONAdaptation();

        public ValidatorBuilder failEarly() {
            this.failEarly = true;
            return this;
        }

        public ValidatorBuilder readWriteContext(ReadWriteContext readWriteContext) {
            this.readWriteContext = readWriteContext;
            return this;
        }

        public ValidatorBuilder jsonAdaptation(JsonAdaptation jsonAdaptation) {
            this.jsonAdaptation = jsonAdaptation;
            return this;
        }

        public Validator build() {
            return new DefaultValidator(failEarly, readWriteContext, jsonAdaptation);
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

    private final JsonAdaptation jsonAdaptation;

    DefaultValidator(boolean failEarly, ReadWriteContext readWriteContext, JsonAdaptation jsonAdaptation) {
        this.failEarly = failEarly;
        this.readWriteContext = readWriteContext;
        this.jsonAdaptation = jsonAdaptation;
    }

    @Override public void performValidation(Schema schema, Object input) {
        ValidationFailureReporter failureReporter = createFailureReporter(schema);
        ReadWriteValidator readWriteValidator = ReadWriteValidator.createForContext(readWriteContext, failureReporter);
        ValidatingVisitor visitor = new ValidatingVisitor(input, failureReporter, readWriteValidator, jsonAdaptation);
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
