package org.everit.json.schema.loader.internal;

import org.everit.json.schema.FormatValidator;

import java8.util.Optional;
import org.everit.json.schema.internal.AFormatValidator;

import static java8.util.Objects.requireNonNull;

public class WrappingFormatValidator extends AFormatValidator {

    private final String formatName;
    private final FormatValidator formatValidator;

    public WrappingFormatValidator(String formatName, FormatValidator wrappedValidator) {
        this.formatName = requireNonNull(formatName, "formatName cannot be null");
        this.formatValidator = requireNonNull(wrappedValidator, "wrappedValidator cannot be null");
    }

    @Override
    public Optional<String> validate(String subject) {
        return formatValidator.validate(subject);
    }

    @Override
    public String formatName() {
        return formatName;
    }
}
