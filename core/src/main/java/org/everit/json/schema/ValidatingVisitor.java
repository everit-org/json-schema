package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

class ValidatingVisitor extends Visitor {

    static class FailureCollector {

        private List<ValidationException> failures = new ArrayList<>(1);

        private Schema schema;

        private FailureCollector(Schema schema) {
            this.schema = schema;
        }

        private FailureCollector(Schema schema, List<ValidationException> failures) {
            this.failures = requireNonNull(failures, "failures cannot be null");
            this.schema = requireNonNull(schema, "schema cannot be null");
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
        arraySchema.accept(new ArraySchemaValidatingVisitor(subject, this, failureCollector));
    }

    @Override void visitBooleanSchema(BooleanSchema schema) {
        if (!(subject instanceof Boolean)) {
            failureCollector.failure(Boolean.class, subject);
        }
    }

    @Override void visitNullSchema(NullSchema nullSchema) {
        if (!(subject == null || subject == JSONObject.NULL)) {
            failureCollector.failure("expected: null, found: " + subject.getClass().getSimpleName(), "type");
        }
    }

    @Override void visit(Schema schema) {
        int failureCountBefore = failureCollector.failures.size();
        super.visit(schema);
        int failureCountAfter = failureCollector.failures.size();
        if (failureCountAfter > failureCountBefore) {
            int newFailureCount = failureCountAfter - failureCountBefore;
            List<ValidationException> newFailures = new ArrayList<>(newFailureCount);
            for (int i = failureCountBefore; i < failureCountAfter; ++i) {
                newFailures.add(failureCollector.failures.get(i));
            }
            while (newFailureCount-- > 0) {
                failureCollector.failures.remove(failureCollector.failures.size() - 1);
            }
            try {
                ValidationException.throwFor(schema, newFailures);
            } catch (ValidationException e) {
                failureCollector.failure(e);
            }
        }
    }

    ValidationException getFailureOfSchema(Schema schema, Object input) {
        int failureCountBefore = failureCollector.failures.size();
        FailureCollector origCollector = this.failureCollector;

        this.failureCollector = failureCollector.subCollectorFor(schema);
        Object origSubject = this.subject;
        this.subject = input;
        visit(schema);
        this.subject = origSubject;
        this.failureCollector = origCollector;

        int failureCountAfter = failureCollector.failures.size();
        int newFailureCount = failureCountAfter - failureCountBefore;
        if (newFailureCount == 0) {
            return null;
        } else if (newFailureCount == 1) {
            return failureCollector.failures.remove(failureCollector.failures.size() - 1);
        } else {
            throw new RuntimeException("found " + newFailureCount + " failures, expected 0 or 1");
        }
    }

    void failIfErrorFound() {
        failureCollector.throwExceptionIfFailureFound();
    }

}
