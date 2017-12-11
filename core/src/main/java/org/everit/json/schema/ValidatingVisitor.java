package org.everit.json.schema;

import java.util.ArrayList;
import java.util.List;

class ValidatingVisitor extends Visitor {

    static class FailureCollector {

        private List<ValidationException> failures = new ArrayList<>(1);

        private Schema schema;

        private FailureCollector(Schema schema) {
            this.schema = schema;
        }

        private FailureCollector(Schema schema, List<ValidationException> failures) {
            this.failures = failures;
            this.schema = schema;
        }

        FailureCollector subCollectorFor(Schema schema) {
            return new FailureCollector(schema, failures);
        }

        void failure(String message, String keyword) {
            failures.add(new ValidationException(schema, message, keyword, schema.getSchemaLocation()));
        }

        void failure(Class<?> expectedType, Object actualValue) {
            failures.add(new ValidationException(schema, expectedType, actualValue, "type", schema.getSchemaLocation()));
        }

        void failure(ValidationException exc) {
            failures.add(exc);
        }

        void throwExceptionIfFailureFound() {
            ValidationException.throwFor(schema, failures);
        }

    }

    protected Object subject;

    protected FailureCollector failureCollector;

    ValidatingVisitor(Object subject, Schema schema) {
        this.subject = subject;
        this.failureCollector = new FailureCollector(schema);
    }

    @Override void visitNumberSchema(NumberSchema numberSchema) {
        numberSchema.accept(new NumberSchemaValidatingVisitor(subject, failureCollector));
    }

    @Override void visitArraySchema(ArraySchema arraySchema) {
        arraySchema.accept(new ArraySchemaValidatingVisitor(subject, failureCollector));
    }

    void failIfErrorFound() {
        failureCollector.throwExceptionIfFailureFound();
    }
}
