package org.everit.json.schema;


import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public class ConditionalSchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private final ValidatingVisitor owner;

    public ConditionalSchemaValidatingVisitor(Object subject, ValidatingVisitor owner) {
        this.subject = subject;
        this.owner = requireNonNull(owner, "owner cannot be null");
    }

    @Override
    void visitConditionalSchema(ConditionalSchema conditionalSchema) {
        if (!conditionalSchema.getIfSchema().isPresent() ||
                (!conditionalSchema.getThenSchema().isPresent() && !conditionalSchema.getElseSchema().isPresent())) {
            return;
        }
        ValidationException ifSchemaException = owner.getFailureOfSchema(conditionalSchema.getIfSchema().get(), subject);
        if (ifSchemaException == null) {
            visitThenSchema(conditionalSchema);
        } else {
            visitElseSchema(conditionalSchema, ifSchemaException);
        }
    }

    private void visitThenSchema(ConditionalSchema conditionalSchema) {
        if (conditionalSchema.getThenSchema().isPresent()) {
            ValidationException thenSchemaException = owner.getFailureOfSchema(conditionalSchema.getThenSchema().get(), subject);
            if (thenSchemaException != null) {
                owner.failure(new ValidationException(conditionalSchema,
                        new StringBuilder(new StringBuilder("#")),
                        "Data is invalid for schema of \"then\" ",
                        Arrays.asList(thenSchemaException),
                        "then",
                        conditionalSchema.getSchemaLocation()));
            }
        }
    }

    private void visitElseSchema(ConditionalSchema conditionalSchema, ValidationException ifSchemaException) {
        if (conditionalSchema.getElseSchema().isPresent()) {
            ValidationException elseSchemaException = owner.getFailureOfSchema(conditionalSchema.getElseSchema().get(), subject);
            if (elseSchemaException != null) {
                owner.failure(new ValidationException(conditionalSchema,
                        new StringBuilder(new StringBuilder("#")),
                        "Data is invalid for schema of both \"if\" and \"else\" ",
                        Arrays.asList(ifSchemaException, elseSchemaException),
                        "else",
                        conditionalSchema.getSchemaLocation()));
            }
        }
    }

}
