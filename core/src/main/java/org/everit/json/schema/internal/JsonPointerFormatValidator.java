package org.everit.json.schema.internal;

import static java.lang.String.format;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;
import org.json.JSONPointer;

public class JsonPointerFormatValidator implements FormatValidator {

    @Override public Optional<String> validate(String subject) {
        try {
            new JSONPointer(subject);
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.of(format("[%s] is not a valid JSON pointer", subject));
        }
    }

    @Override public String formatName() {
        return "json-pointer";
    }
}
