package org.everit.json.schema;

import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.listener.ConditionalSchemaValidationEvent.Keyword.IF;
import static org.everit.json.schema.listener.ConditionalSchemaValidationEvent.Keyword.THEN;

import java.util.Arrays;

import org.everit.json.schema.listener.ConditionalSchemaMatchEvent;
import org.everit.json.schema.listener.ConditionalSchemaMismatchEvent;

class ConditionalSchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private final ValidatingVisitor owner;

    private ConditionalSchema conditionalSchema;

    private ValidationException ifSchemaException;

    ConditionalSchemaValidatingVisitor(Object subject, ValidatingVisitor owner) {
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
        owner.validationListener.ifSchemaMatch(new ConditionalSchemaMatchEvent(conditionalSchema, subject, IF));
    }

    @Override
    void visitThenSchema(Schema thenSchema) {
        if (ifSchemaException == null) {
            ValidationException thenSchemaException = owner.getFailureOfSchema(thenSchema, subject);
            if (thenSchemaException != null) {
                ValidationException failure = new ValidationException(conditionalSchema,
                        new StringBuilder(new StringBuilder("#")),
                        "input is invalid against the \"then\" schema",
                        Arrays.asList(thenSchemaException),
                        "then",
                        conditionalSchema.getSchemaLocation());

                owner.validationListener
                        .thenSchemaMismatch(new ConditionalSchemaMismatchEvent(conditionalSchema, subject, THEN, thenSchemaException));
                owner.failure(failure);
            } else {
                owner.validationListener.thenSchemaMatch(new ConditionalSchemaMatchEvent(conditionalSchema, subject, THEN));
            }
        }
    }

    @Override
    void visitElseSchema(Schema elseSchema) {
        if (ifSchemaException != null) {
            ValidationException elseSchemaException = owner.getFailureOfSchema(elseSchema, subject);
            if (elseSchemaException != null) {
                ValidationException failure = new ValidationException(conditionalSchema,
                        new StringBuilder(new StringBuilder("#")),
                        "input is invalid against both the \"if\" and \"else\" schema",
                        Arrays.asList(ifSchemaException, elseSchemaException),
                        "else",
                        conditionalSchema.getSchemaLocation());

                //                owner.reportSchemaMatchEvent(elseSchema, failure);
                owner.failure(failure);
            }
            //            owner.reportSchemaMatchEvent(elseSchema, null);
        }
    }

}
