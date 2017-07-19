package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;

public class JsonPointerFormatValidator implements FormatValidator {

    @Override public Optional<String> validate(String subject) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override public String formatName() {
        return "json-pointer";
    }
}
