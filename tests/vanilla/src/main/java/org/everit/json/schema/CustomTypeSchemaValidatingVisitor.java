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

    void visitCustomTypeSchema(CustomTestSchema customTestSchema) {
        this.customTestSchema = customTestSchema;
        if (owner.passesTypeCheck(String.class, true, false)) {
            rightValueSubject = (String) subject;
            visitRightValue(customTestSchema.rightValue());
        }
    }
    
    void visitRightValue(String rightValue) {
        if(rightValue != null && !rightValueSubject.equals(rightValue)) {
            ValidationException violation = new ValidationException(customTestSchema,"'"+rightValue+"' is not the right value ('"+rightValueSubject+"')");
            owner.failure(violation);
        }
    }
}
