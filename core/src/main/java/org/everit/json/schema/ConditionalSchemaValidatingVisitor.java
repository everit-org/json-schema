package org.everit.json.schema;


import java.util.Arrays;

import static java.util.Objects.requireNonNull;

class ConditionalSchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private final ValidatingVisitor owner;

    private ConditionalSchema conditionalSchema;

    private ValidationException ifSchemaException;

    public ConditionalSchemaValidatingVisitor(Object subject, ValidatingVisitor owner) {
        this.subject = subject;
        this.owner = requireNonNull(owner, "owner cannot be null");
    }

    @Override
    void visitConditionalSchema(ConditionalSchema conditionalSchema) {
        this.conditionalSchema = conditionalSchema;
        if (!conditionalSchema.getIfSchema().isPresent() ||
                (!conditionalSchema.getThenSchema().isPresent() && !conditionalSchema.getElseSchema().isPresent())) {
            return;
        }
        super.visitConditionalSchema(conditionalSchema);
    }

    @Override
    void visitIfSchema(Schema ifSchema) {
        if (conditionalSchema.getIfSchema().isPresent()) {
            ifSchemaException = owner.getFailureOfSchema(ifSchema, subject);
        }
    }

    @Override
    void visitThenSchema(Schema thenSchema) {
        if (ifSchemaException == null) {
            ValidationException thenSchemaException = owner.getFailureOfSchema(thenSchema, subject);
            if (thenSchemaException != null) {
                owner.failure(new ValidationException(conditionalSchema,
                        new StringBuilder(new StringBuilder("#")),
                        "input is invalid against the \"then\" schema",
                        Arrays.asList(thenSchemaException),
                        "then",
                        conditionalSchema.getSchemaLocation()));
            }
        }
    }

    @Override
    void visitElseSchema(Schema elseSchema) {
        if (ifSchemaException != null) {
            ValidationException elseSchemaException = owner.getFailureOfSchema(elseSchema, subject);
            if (elseSchemaException != null) {
                owner.failure(new ValidationException(conditionalSchema,
                        new StringBuilder(new StringBuilder("#")),
                        "input is invalid against both the \"if\" and \"else\" schema",
                        Arrays.asList(ifSchemaException, elseSchemaException),
                        "else",
                        conditionalSchema.getSchemaLocation()));
            }
        }
    }

}
