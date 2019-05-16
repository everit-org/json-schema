package org.everit.json.schema.loader.internal;

import org.everit.json.schema.AbstractFormatValidator;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class WrappingFormatValidator implements AbstractFormatValidator {

    private final String formatName;
    private final AbstractFormatValidator formatValidator;

    public WrappingFormatValidator(String formatName, AbstractFormatValidator wrappedValidator) {
        this.formatName = requireNonNull(formatName, "formatName cannot be null");
        this.formatValidator = requireNonNull(wrappedValidator, "wrappedValidator cannot be null");
    }


    @Override
    public Optional<String> validate(String subject) {
        return formatValidator.validate(subject);
    }

    @Override
    public Optional<String> validate(String subject, Map<String, Object> unprocessedProperties) {
        return formatValidator.validate(subject, unprocessedProperties);
    }

    @Override
    public String formatName() {
        return formatName;
    }
}
