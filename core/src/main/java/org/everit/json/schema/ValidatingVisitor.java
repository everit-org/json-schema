package org.everit.json.schema;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static org.everit.json.schema.EnumSchema.toJavaValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.everit.json.schema.event.CombinedSchemaMatchEvent;
import org.everit.json.schema.event.CombinedSchemaMismatchEvent;
import org.everit.json.schema.event.SchemaReferencedEvent;
import org.everit.json.schema.event.ValidationListener;
import org.json.JSONArray;
import org.json.JSONObject;

class ValidatingVisitor extends Visitor {

    private static final List<Class<?>> VALIDATED_TYPES = unmodifiableList(asList(
            Number.class,
            String.class,
            Boolean.class,
            JSONObject.class,
            JSONArray.class,
            JSONObject.NULL.getClass()
    ));

    static final String TYPE_FAILURE_MSG = "subject is an instance of non-handled type %s. Should be one of "
            + VALIDATED_TYPES.stream().map(Class::getSimpleName).collect(joining(", "));

    private static boolean isNull(Object obj) {
        return obj == null || JSONObject.NULL.equals(obj);
    }

    protected Object subject;

    final ValidationListener validationListener;

    private ValidationFailureReporter failureReporter;

    private final ReadWriteValidator readWriteValidator;

    @Override
    void visit(Schema schema, List<String> path) {
        if (schema.isNullable() == Boolean.FALSE && isNull(subject)) {
            failureReporter.failure("value cannot be null", "nullable");
        }
        readWriteValidator.validate(schema, subject);
        super.visit(schema, path);
    }

    ValidatingVisitor(Object subject, ValidationFailureReporter failureReporter, ReadWriteValidator readWriteValidator,
            ValidationListener validationListener) {
        if (subject != null && !VALIDATED_TYPES.stream().anyMatch(type -> type.isAssignableFrom(subject.getClass()))) {
            throw new IllegalArgumentException(format(TYPE_FAILURE_MSG, subject.getClass().getSimpleName()));
        }
        this.subject = subject;
        this.failureReporter = failureReporter;
        this.readWriteValidator = readWriteValidator;
        this.validationListener = validationListener;
    }

    @Override
    void visitNumberSchema(NumberSchema numberSchema) {
        numberSchema.accept(new NumberSchemaValidatingVisitor(subject, this));
    }

    @Override
    void visitArraySchema(ArraySchema arraySchema, List<String> path) {
        arraySchema.accept(new ArraySchemaValidatingVisitor(subject, this), path);
    }

    @Override
    void visitBooleanSchema(BooleanSchema schema) {
        if (!(subject instanceof Boolean)) {
            failureReporter.failure(Boolean.class, subject);
        }
    }

    @Override
    void visitNullSchema(NullSchema nullSchema) {
        if (!(subject == null || subject == JSONObject.NULL)) {
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
    void visitNotSchema(NotSchema notSchema, List<String> path) {
        Schema mustNotMatch = notSchema.getMustNotMatch();
        ValidationException failure = getFailureOfSchema(mustNotMatch, subject, path);
        if (failure == null) {
            failureReporter.failure("subject must not be valid against schema " + mustNotMatch, "not");
        }
    }

    @Override
    void visitReferenceSchema(ReferenceSchema referenceSchema, List<String> path) {
        Schema referredSchema = referenceSchema.getReferredSchema();
        if (referredSchema == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }
        ValidationException failure = getFailureOfSchema(referredSchema, subject, path);
        if (failure != null) {
            failureReporter.failure(failure);
        }
        else if (validationListener != null) {
            validationListener.schemaReferenced(new SchemaReferencedEvent(referenceSchema, subject, referredSchema, path));
        }
    }

    @Override
    void visitObjectSchema(ObjectSchema objectSchema, List<String> path) {
        objectSchema.accept(new ObjectSchemaValidatingVisitor(subject, this), path);
    }

    @Override
    void visitStringSchema(StringSchema stringSchema) {
        stringSchema.accept(new StringSchemaValidatingVisitor(subject, this));
    }

    @Override
    void visitCombinedSchema(CombinedSchema combinedSchema, List<String> path) {
        Collection<Schema> subschemas = combinedSchema.getSubschemas();
        List<ValidationException> failures = new ArrayList<>(subschemas.size());
        CombinedSchema.ValidationCriterion criterion = combinedSchema.getCriterion();
        for (Schema subschema : subschemas) {
            ValidationException exception = getFailureOfSchema(subschema, subject, path);
            if (null != exception) {
                failures.add(exception);
            }
            reportSchemaMatchEvent(combinedSchema, subschema, exception, path);
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
    void visitConditionalSchema(ConditionalSchema conditionalSchema, List<String> path) {
        conditionalSchema.accept(new ConditionalSchemaValidatingVisitor(subject, this), path);
    }

    private void reportSchemaMatchEvent(CombinedSchema schema, Schema subschema, ValidationException failure, List<String> path) {
        if (failure == null) {
            validationListener.combinedSchemaMatch(new CombinedSchemaMatchEvent(schema, subschema, subject, path));
        } else {
            validationListener.combinedSchemaMismatch(new CombinedSchemaMismatchEvent(schema, subschema, subject, failure, path));
        }
    }

    ValidationException getFailureOfSchema(Schema schema, Object input, List<String> path) {
        Object origSubject = this.subject;
        this.subject = input;
        ValidationException rval = failureReporter.inContextOfSchema(schema, () -> visit(schema, path));
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
