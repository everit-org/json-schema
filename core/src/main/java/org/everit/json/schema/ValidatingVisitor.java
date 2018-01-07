package org.everit.json.schema;

import static java.lang.String.format;
import static org.everit.json.schema.EnumSchema.toJavaValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;

class ValidatingVisitor extends Visitor {

    private static boolean isNull(Object obj) {
        return obj == null || JSONObject.NULL.equals(obj);
    }

    protected Object subject;

    private ValidationFailureReporter failureReporter;

    ValidatingVisitor(Schema schema, Object subject) {
        this.subject = subject;
        this.failureReporter = new CollectingFailureReporter(schema);
    }

    ValidatingVisitor(Object subject, ValidationFailureReporter failureReporter) {
        this.subject = subject;
        this.failureReporter = failureReporter;
    }

    @Override void visitNumberSchema(NumberSchema numberSchema) {
        numberSchema.accept(new NumberSchemaValidatingVisitor(subject, failureReporter));
    }

    @Override void visitArraySchema(ArraySchema arraySchema) {
        arraySchema.accept(new ArraySchemaValidatingVisitor(subject, this));
    }

    @Override void visitBooleanSchema(BooleanSchema schema) {
        if (!(subject instanceof Boolean)) {
            failureReporter.failure(Boolean.class, subject);
        }
    }

    @Override void visitNullSchema(NullSchema nullSchema) {
        if (!(subject == null || subject == JSONObject.NULL)) {
            failureReporter.failure("expected: null, found: " + subject.getClass().getSimpleName(), "type");
        }
    }

    @Override void visitConstSchema(ConstSchema constSchema) {
        if (isNull(subject) && isNull(constSchema.getPermittedValue())) {
            return;
        }
        Object effectiveSubject = toJavaValue(subject);
        if (!ObjectComparator.deepEquals(effectiveSubject, constSchema.getPermittedValue())) {
            failureReporter.failure("", "const");
        }
    }

    @Override void visitEnumSchema(EnumSchema enumSchema) {
        Object effectiveSubject = toJavaValue(subject);
        for (Object possibleValue : enumSchema.getPossibleValues()) {
            if (ObjectComparator.deepEquals(possibleValue, effectiveSubject)) {
                return;
            }
        }
        failureReporter.failure(format("%s is not a valid enum value", subject), "enum");
    }

    @Override void visitFalseSchema(FalseSchema falseSchema) {
        failureReporter.failure("false schema always fails", "false");
    }

    @Override void visitNotSchema(NotSchema notSchema) {
        Schema mustNotMatch = notSchema.getMustNotMatch();
        ValidationException failure = getFailureOfSchema(mustNotMatch, subject);
        if (failure == null) {
            failureReporter.failure("subject must not be valid against schema " + mustNotMatch, "not");
        }
    }

    @Override void visitReferenceSchema(ReferenceSchema referenceSchema) {
        Schema referredSchema = referenceSchema.getReferredSchema();
        if (referredSchema == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }
        ValidationException failure = getFailureOfSchema(referredSchema, subject);
        if (failure != null) {
            failureReporter.failure(failure);
        }
    }

    @Override void visitObjectSchema(ObjectSchema objectSchema) {
        objectSchema.accept(new ObjectSchemaValidatingVisitor(subject, this));
    }

    @Override void visitStringSchema(StringSchema stringSchema) {
        stringSchema.accept(new StringSchemaValidatingVisitor(subject, failureReporter));
    }

    @Override void visitCombinedSchema(CombinedSchema combinedSchema) {
        List<ValidationException> failures = new ArrayList<>();
        Collection<Schema> subschemas = combinedSchema.getSubschemas();
        CombinedSchema.ValidationCriterion criterion = combinedSchema.getCriterion();
        for (Schema subschema : subschemas) {
            ValidationException exception = getFailureOfSchema(subschema, subject);
            if (null != exception) {
                failures.add(exception);
            }
        }
        int matchingCount = subschemas.size() - failures.size();
        try {
            criterion.validate(subschemas.size(), matchingCount);
        } catch (ValidationException e) {
            failureReporter.failure(new ValidationException(combinedSchema,
                    new StringBuilder(e.getPointerToViolation()),
                    e.getMessage(),
                    failures,
                    e.getKeyword(),
                    combinedSchema.getSchemaLocation()));
        }
    }

    ValidationException getFailureOfSchema(Schema schema, Object input) {
        Object origSubject = this.subject;
        this.subject = input;
        return failureReporter.inContextOfSchema(schema, () -> {
            visit(schema);
            this.subject = origSubject;
        });
    }

    void failIfErrorFound() {
        failureReporter.validationFinished();
    }

    void failure(String message, String keyword) {
        failureReporter.failure(message, keyword);
    }

    void failure(Class<?> expectedType, Object actualValue) {
        failureReporter.failure(expectedType, actualValue);
    }

    void failure(ValidationException exc) {
        failureReporter.failure(exc);
    }

}
