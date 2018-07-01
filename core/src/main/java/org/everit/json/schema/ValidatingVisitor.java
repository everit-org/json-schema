package org.everit.json.schema;

import static java.lang.String.format;
import static org.everit.json.schema.EnumSchema.toJavaValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.everit.json.schema.loader.JsonObject;

import com.fasterxml.jackson.databind.node.NullNode;

class ValidatingVisitor extends Visitor {

    private static boolean isNull(Object obj) {
        return obj == null || JsonObject.NULL.equals(obj);
    }

    protected Object subject;

    private ValidationFailureReporter failureReporter;

    private final ReadWriteValidator readWriteValidator;

    @Override
    void visit(Schema schema) {
        if (schema.isNullable() == Boolean.FALSE && isNull(subject)) {
            failureReporter.failure("value cannot be null", "nullable");
        }
        readWriteValidator.validate(schema, subject);
        super.visit(schema);
    }

    ValidatingVisitor(Object subject, ValidationFailureReporter failureReporter, ReadWriteValidator readWriteValidator) {
        this.subject = subject;
        this.failureReporter = failureReporter;
        this.readWriteValidator = readWriteValidator;
    }

    @Override
    void visitNumberSchema(NumberSchema numberSchema) {
        numberSchema.accept(new NumberSchemaValidatingVisitor(subject, this));
    }

    @Override
    void visitArraySchema(ArraySchema arraySchema) {
        arraySchema.accept(new ArraySchemaValidatingVisitor(subject, this));
    }

    @Override
    void visitBooleanSchema(BooleanSchema schema) {
        if (!(subject instanceof Boolean)) {
            failureReporter.failure(Boolean.class, subject);
        }
    }

    @Override
    void visitNullSchema(NullSchema nullSchema) {
        if (!(subject == null || subject == JsonObject.NULL)) {
            failureReporter.failure("expected: null, found: " + subject.getClass().getSimpleName(), "type");
        }
    }

    @Override
    void visitConstSchema(ConstSchema constSchema) {
        if (isNull(subject) && isNull(constSchema.getPermittedValue())) {
            return;
        }
        Object effectiveSubject = toJavaValue(subject);
        if (!ObjectComparator.deepEquals(effectiveSubject, constSchema.getPermittedValue())) {
            failureReporter.failure("", "const");
        }
    }

    @Override
    void visitEnumSchema(EnumSchema enumSchema) {
        Object effectiveSubject = toJavaValue(subject);
        for (Object possibleValue : enumSchema.getPossibleValues()) {
            if (ObjectComparator.deepEquals(possibleValue, effectiveSubject)) {
                return;
            }
        }
        failureReporter.failure(format("%s is not a valid enum value", subject), "enum");
    }

    @Override
    void visitFalseSchema(FalseSchema falseSchema) {
        failureReporter.failure("false schema always fails", "false");
    }

    @Override
    void visitNotSchema(NotSchema notSchema) {
        Schema mustNotMatch = notSchema.getMustNotMatch();
        ValidationException failure = getFailureOfSchema(mustNotMatch, subject);
        if (failure == null) {
            failureReporter.failure("subject must not be valid against schema " + mustNotMatch, "not");
        }
    }

    @Override
    void visitReferenceSchema(ReferenceSchema referenceSchema) {
        Schema referredSchema = referenceSchema.getReferredSchema();
        if (referredSchema == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }
        ValidationException failure = getFailureOfSchema(referredSchema, subject);
        if (failure != null) {
            failureReporter.failure(failure);
        }
    }

    @Override
    void visitObjectSchema(ObjectSchema objectSchema) {
        objectSchema.accept(new ObjectSchemaValidatingVisitor(subject, this));
    }

    @Override
    void visitStringSchema(StringSchema stringSchema) {
        stringSchema.accept(new StringSchemaValidatingVisitor(subject, this));
    }

    @Override
    void visitCombinedSchema(CombinedSchema combinedSchema) {
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

    @Override
    void visitConditionalSchema(ConditionalSchema conditionalSchema) {
        conditionalSchema.accept(new ConditionalSchemaValidatingVisitor(subject, this));
    }

    ValidationException getFailureOfSchema(Schema schema, Object input) {
        Object origSubject = this.subject;
        this.subject = input;
        ValidationException rval = failureReporter.inContextOfSchema(schema, () -> visit(schema));
        this.subject = origSubject;
        return rval;
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

    boolean passesTypeCheck(Class<?> expectedType, boolean schemaRequiresType, Boolean nullable) {
        if (isNull(subject)) {
            if (schemaRequiresType && nullable != Boolean.TRUE) {
                failureReporter.failure(expectedType, subject);
            }
            return false;
        }
        if (expectedType.isAssignableFrom(subject.getClass())) {
            return true;
        }
        if (schemaRequiresType) {
            failureReporter.failure(expectedType, subject);
        }
        return false;
    }
}
