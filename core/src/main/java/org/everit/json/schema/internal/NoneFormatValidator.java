package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;

public class NoneFormatValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        return Optional.empty();
    }

    @Override
    public String formatName() {
        return "NONE";
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof NoneFormatValidator;
    }

    @Override
    public int hashCode() {
        return NoneFormatValidator.class.hashCode();
    }
}
