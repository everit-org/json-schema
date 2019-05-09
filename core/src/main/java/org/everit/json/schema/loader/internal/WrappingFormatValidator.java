package org.everit.json.schema.loader.internal;

import org.everit.json.schema.FormatValidator;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class WrappingFormatValidator implements FormatValidator {

    private final String formatName;
    private final FormatValidator formatValidator;

    public WrappingFormatValidator(String formatName, FormatValidator wrappedValidator) {
        this.formatName = requireNonNull(formatName, "formatName cannot be null");
        this.formatValidator = requireNonNull(wrappedValidator, "wrappedValidator cannot be null");
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
