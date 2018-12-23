package org.everit.json.schema;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.ELSE;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.IF;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.THEN;

import org.everit.json.schema.event.ConditionalSchemaMatchEvent;
import org.everit.json.schema.event.ConditionalSchemaMismatchEvent;
import org.everit.json.schema.event.ConditionalSchemaValidationEvent;

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
            if (ifSchemaException == null) {
                owner.validationListener.ifSchemaMatch(createMatchEvent(IF));
            } else {
                owner.validationListener.ifSchemaMismatch(createMismatchEvent(IF, ifSchemaException));
            }
        }
    }

    @Override
    void visitThenSchema(Schema thenSchema) {
        if (ifSchemaException == null) {
            ValidationException thenSchemaException = owner.getFailureOfSchema(thenSchema, subject);
            if (thenSchemaException != null) {
                ValidationException failure = new ValidationException(conditionalSchema,
                        new StringBuilder(new StringBuilder("#")),
                        "input is invalid against the \"then\" schema",
                        asList(thenSchemaException),
                        "then",
                        conditionalSchema.getSchemaLocation());

                owner.validationListener.thenSchemaMismatch(createMismatchEvent(THEN, thenSchemaException));
                owner.failure(failure);
            } else {
                owner.validationListener.thenSchemaMatch(createMatchEvent(THEN));
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
                        asList(ifSchemaException, elseSchemaException),
                        "else",
                        conditionalSchema.getSchemaLocation());
                owner.validationListener.elseSchemaMismatch(createMismatchEvent(ELSE, elseSchemaException));
                owner.failure(failure);
            } else {
                owner.validationListener.elseSchemaMatch(createMatchEvent(ELSE));
            }
        }
    }

    private ConditionalSchemaMatchEvent createMatchEvent(ConditionalSchemaValidationEvent.Keyword keyword) {
        return new ConditionalSchemaMatchEvent(conditionalSchema, subject, keyword);
    }

    private ConditionalSchemaMismatchEvent createMismatchEvent(ConditionalSchemaValidationEvent.Keyword keyword,
            ValidationException failure) {
        return new ConditionalSchemaMismatchEvent(conditionalSchema, subject, keyword, failure);
    }

}
