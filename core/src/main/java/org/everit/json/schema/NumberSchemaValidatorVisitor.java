package org.everit.json.schema;

class NumberSchemaValidatorVisitor extends Visitor {

    private final Object subject;

    private final ValidatingVisitor.FailureCollector failureCollector;

    private boolean exclusiveMinimum;

    private double numberSubject;

    NumberSchemaValidatorVisitor(Object subject, ValidatingVisitor.FailureCollector failureCollector) {
        this.subject = subject;
        this.failureCollector = failureCollector;
    }

    @Override void visitNumberSchema(NumberSchema numberSchema) {
        if (!(subject instanceof Number)) {
            if (numberSchema.isRequiresNumber()) {
                failureCollector.failure(Number.class, subject);
            }
        } else {
            if (!(subject instanceof Integer || subject instanceof Long) && numberSchema.requiresInteger()) {
                failureCollector.failure(Integer.class, subject);
            } else {
                this.numberSubject = ((Number) subject).doubleValue();
                super.visitNumberSchema(numberSchema);
            }
        }
    }

    @Override void visitExclusiveMinimum(boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override void visitMinimum(Number minimum) {
        if (minimum == null) {
            return;
        }
        if (exclusiveMinimum && numberSubject <= minimum.doubleValue()) {
            failureCollector.failure(subject + " is not greater than " + minimum, "exclusiveMinimum");
        } else if (numberSubject < minimum.doubleValue()) {
            failureCollector.failure(subject + " is not greater or equal to " + minimum, "minimum");
        }
    }

    @Override void visitExclusiveMinimumLimit(Number exclusiveMinimumLimit) {
        if (exclusiveMinimumLimit != null) {
            if (numberSubject <= exclusiveMinimumLimit.doubleValue()) {
                failureCollector.failure(subject + " is not greater than " + exclusiveMinimumLimit, "exclusiveMinimum");
            }
        }
    }
}
