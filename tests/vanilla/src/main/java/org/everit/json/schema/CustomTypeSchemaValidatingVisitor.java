package org.everit.json.schema;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.everit.json.schema.regexp.Regexp;

public class CustomTypeSchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private final ValidatingVisitor owner;
    
    private CustomTestSchema customTestSchema;
    
    private String rightValueSubject;

    public CustomTypeSchemaValidatingVisitor(Object subject, ValidatingVisitor owner) {
        this.subject = subject;
        this.owner = requireNonNull(owner, "failureReporter cannot be null");
    }

    void visitCustomTypeSchema(AbstractCustomTypeSchema customTestSchema) {
        this.customTestSchema = (CustomTestSchema)customTestSchema;
        if (owner.passesTypeCheck(String.class, true, false)) {
            rightValueSubject = (String) subject;
            visitRightValue(this.customTestSchema.rightValue());
        }
    }
    
    void visitRightValue(String rightValue) {
        if(rightValue != null && !rightValueSubject.equals(rightValue)) {
            ValidationException violation = new ValidationException(customTestSchema,"'"+rightValueSubject+"' is not the right value ('"+rightValue+"')");
            owner.failure(violation);
        }
    }
}
