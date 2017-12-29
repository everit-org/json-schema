package org.everit.json.schema;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.regex.Pattern;

public class StringSchemaValidatingVisitor extends Visitor {

    private final Object subject;

    private String stringSubject;

    private int stringLength;

    private final ValidationFailureReporter failureReporter;

    public StringSchemaValidatingVisitor(Object subject, ValidationFailureReporter failureReporter) {
        this.subject = subject;
        this.failureReporter = requireNonNull(failureReporter, "failureReporter cannot be null");
    }

    @Override void visitStringSchema(StringSchema stringSchema) {
        if (!(subject instanceof String)) {
            if (stringSchema.requireString()) {
                failureReporter.failure(String.class, subject);
            }
        } else {
            stringSubject = (String) subject;
            stringLength = stringSubject.codePointCount(0, stringSubject.length());
            super.visitStringSchema(stringSchema);
        }
    }

    @Override void visitMinLength(Integer minLength) {
        if (minLength != null && stringLength < minLength.intValue()) {
            failureReporter.failure("expected minLength: " + minLength + ", actual: "
                    + stringLength, "minLength");
        }
    }

    @Override void visitMaxLength(Integer maxLength) {
        if (maxLength != null && stringLength > maxLength.intValue()) {
            failureReporter.failure("expected maxLength: " + maxLength + ", actual: "
                    + stringLength, "maxLength");
        }
    }

    @Override void visitPattern(Pattern pattern) {
        if (pattern != null && !pattern.matcher(stringSubject).find()) {
            String message = format("string [%s] does not match pattern %s",
                    subject, pattern.pattern());
            failureReporter.failure(message, "pattern");
        }
    }

    @Override void visitFormat(FormatValidator formatValidator) {
        Optional<String> failure = formatValidator.validate(stringSubject);
        if (failure.isPresent()) {
            failureReporter.failure(failure.get(), "format");
        }
    }
}
