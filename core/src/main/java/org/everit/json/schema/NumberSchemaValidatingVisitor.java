package org.everit.json.schema;

import java.math.BigDecimal;

import static org.everit.json.schema.NumberComparator.compare;
import static org.everit.json.schema.NumberComparator.getAsBigDecimal;


class NumberSchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private final ValidatingVisitor owner;

    private boolean exclusiveMinimum;

    private boolean exclusiveMaximum;

    private Number numberSubject;

    NumberSchemaValidatingVisitor(Object subject, ValidatingVisitor owner) {
        this.subject = subject;
        this.owner = owner;
    }

    @Override
    void visitNumberSchema(NumberSchema numberSchema) {
        Class<? extends Number> expectedType = numberSchema.requiresInteger() ? Integer.class : Number.class;
        boolean schemaRequiresType = numberSchema.requiresInteger() || numberSchema.isRequiresNumber();
        owner.ifPassesTypeCheck(expectedType, Number.class::cast, schemaRequiresType,
                numberSchema.isNullable(),
                numberSubject -> {
                    this.numberSubject = numberSubject;
                    super.visitNumberSchema(numberSchema);
                });
    }

    @Override
    void visitExclusiveMinimum(boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override
    void visitMinimum(Number minimum) {
        if (minimum == null) {
            return;
        }
        int comparison = compare(numberSubject, minimum);
        if (exclusiveMinimum && comparison <= 0) {
            owner.failure(subject + " is not greater than " + minimum, "exclusiveMinimum");
        } else if (comparison < 0) {
            owner.failure(subject + " is not greater or equal to " + minimum, "minimum");
        }
    }

    @Override
    void visitExclusiveMinimumLimit(Number exclusiveMinimumLimit) {
        if (exclusiveMinimumLimit != null) {
            if (compare(numberSubject, exclusiveMinimumLimit) <= 0) {
                owner.failure(subject + " is not greater than " + exclusiveMinimumLimit, "exclusiveMinimum");
            }
        }
    }

    @Override
    void visitMaximum(Number maximum) {
        if (maximum == null) {
            return;
        }
        int comparison = compare(maximum, numberSubject);
        if (exclusiveMaximum && comparison <= 0) {
            owner.failure(subject + " is not less than " + maximum, "exclusiveMaximum");
        } else if (comparison < 0) {
            owner.failure(subject + " is not less or equal to " + maximum, "maximum");
        }
    }

    @Override
    void visitExclusiveMaximum(boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    @Override
    void visitExclusiveMaximumLimit(Number exclusiveMaximumLimit) {
        if (exclusiveMaximumLimit != null) {
            if (compare(numberSubject, exclusiveMaximumLimit) >= 0) {
                owner.failure(subject + " is not less than " + exclusiveMaximumLimit, "exclusiveMaximum");
            }
        }
    }

    @Override
    void visitMultipleOf(Number multipleOf) {
        if (multipleOf != null) {
            BigDecimal remainder = getAsBigDecimal(numberSubject).remainder(
                getAsBigDecimal(multipleOf));
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                owner.failure(subject + " is not a multiple of " + multipleOf, "multipleOf");
            }
        }
    }
}
