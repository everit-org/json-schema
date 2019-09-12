package org.everit.json.schema;

import java.util.List;

class InternalValidationException extends ValidationException {

    InternalValidationException(Schema violatedSchema, Class<?> expectedType, Object actualValue) {
        super(violatedSchema, expectedType, actualValue);
    }

    InternalValidationException(Schema violatedSchema, Class<?> expectedType, Object actualValue, String keyword,
            String schemaLocation) {
        super(violatedSchema, expectedType, actualValue, keyword, schemaLocation);
    }

    InternalValidationException(Schema violatedSchema, String message, String keyword, String schemaLocation) {
        super(violatedSchema, message, keyword, schemaLocation);
    }

    InternalValidationException(Schema violatedSchema, StringBuilder pointerToViolation, String message,
            List<ValidationException> causingExceptions, String keyword, String schemaLocation) {
        super(violatedSchema, pointerToViolation, message, causingExceptions, keyword, schemaLocation);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
