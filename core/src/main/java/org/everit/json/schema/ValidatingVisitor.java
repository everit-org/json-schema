package org.everit.json.schema;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.everit.json.schema.EnumSchema.toJavaValue;
import static org.everit.json.schema.PrimitiveValidationStrategy.LENIENT;
import static org.everit.json.schema.StringToValueConverter.stringToValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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

    private final PrimitiveValidationStrategy primitiveValidationStrategy;

    @Override
    void visit(Schema schema) {
        if (Boolean.FALSE.equals(schema.isNullable()) && isNull(subject)) {
            failureReporter.failure("value cannot be null", "nullable");
        }
        readWriteValidator.validate(schema, subject);
        super.visit(schema);
    }

    ValidatingVisitor(Object subject, ValidationFailureReporter failureReporter, ReadWriteValidator readWriteValidator,
                      ValidationListener validationListener,
                      PrimitiveValidationStrategy primitiveValidationStrategy) {
        if (subject != null && !VALIDATED_TYPES.stream().anyMatch(type -> type.isAssignableFrom(subject.getClass()))) {
            throw new IllegalArgumentException(format(TYPE_FAILURE_MSG, subject.getClass().getSimpleName()));
        }
        this.subject = subject;
        this.failureReporter = failureReporter;
        this.readWriteValidator = readWriteValidator;
        this.validationListener = validationListener;
        this.primitiveValidationStrategy = requireNonNull(primitiveValidationStrategy);
    }

    @Override
    void visitNumberSchema(NumberSchema numberSchema) {
        numberSchema.accept(new NumberSchemaValidatingVisitor(subject, this));
    }

    @Override
    void visitArraySchema(ArraySchema arraySchema) {
        arraySchema.accept(new ArraySchemaValidatingVisitor(this));
    }

    @Override
    void visitBooleanSchema(BooleanSchema schema) {
        ifPassesTypeCheck(Boolean.class, true, schema.isNullable(), v -> {});
    }

    @Override
    void visitNullSchema(NullSchema nullSchema) {
        if (!(isNull(subject) || (primitiveValidationStrategy == LENIENT && "null".equals(subject)))) {
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
        else if (validationListener != null) {
            validationListener.schemaReferenced(new SchemaReferencedEvent(referenceSchema, subject, referredSchema));
        }
    }

    @Override
    void visitObjectSchema(ObjectSchema objectSchema) {
        objectSchema.accept(new ObjectSchemaValidatingVisitor(this));
    }

    @Override
    void visitStringSchema(StringSchema stringSchema) {
        stringSchema.accept(new StringSchemaValidatingVisitor(subject, this));
    }

    @Override
    void visitCombinedSchema(CombinedSchema combinedSchema) {
        Collection<Schema> subschemas = combinedSchema.getSubschemas();
        List<ValidationException> failures = new ArrayList<>(subschemas.size());
        CombinedSchema.ValidationCriterion criterion = combinedSchema.getCriterion();
        for (Schema subschema : subschemas) {
            ValidationException exception = getFailureOfSchema(subschema, subject);
            if (null != exception) {
                failures.add(exception);
            }
            reportSchemaMatchEvent(combinedSchema, subschema, exception);
        }
        int matchingCount = subschemas.size() - failures.size();
        try {
            criterion.validate(subschemas.size(), matchingCount);
        } catch (ValidationException e) {
            failureReporter.failure(new InternalValidationException(combinedSchema,
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

    private void reportSchemaMatchEvent(CombinedSchema schema, Schema subschema, ValidationException failure) {
        if (failure == null) {
            validationListener.combinedSchemaMatch(new CombinedSchemaMatchEvent(schema, subschema, subject));
        } else {
            validationListener.combinedSchemaMismatch(new CombinedSchemaMismatchEvent(schema, subschema, subject, failure));
        }
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

    Object getFailureState() {
        return failureReporter.getState();
    }

    boolean isFailureStateChanged(Object olState) {
        return failureReporter.isChanged(olState);
    }

    <SE, E extends SE> void ifPassesTypeCheck(Class<E> expectedType, Function<Object, SE> castFn, boolean schemaRequiresType, Boolean nullable,
                                              Consumer<SE> onPass) {
        Object subject = this.subject;
        if (primitiveValidationStrategy == LENIENT) {
            boolean expectedString = expectedType.isAssignableFrom(String.class);
            if (subject instanceof String && !expectedString) {
                subject = stringToValue((String) subject);
            } else if (expectedString) {
                subject = subject.toString();
            }
        }
        if (isNull(subject)) {
            if (schemaRequiresType && !Boolean.TRUE.equals(nullable)) {
                failureReporter.failure(expectedType, this.subject);
            }
            return;
        }
        if (TypeChecker.isAssignableFrom(expectedType, subject.getClass())) {
            onPass.accept(castFn.apply(subject));
            return;
        }
        if (schemaRequiresType) {
            failureReporter.failure(expectedType, this.subject);
        }
    }

    <E> void ifPassesTypeCheck(Class<E> expectedType, boolean schemaRequiresType, Boolean nullable,
                               Consumer<E> onPass) {
        ifPassesTypeCheck(expectedType, expectedType::cast, schemaRequiresType, nullable, onPass);
    }
}
