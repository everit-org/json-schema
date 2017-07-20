package org.everit.json.schema.internal;

import static java.lang.String.format;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;
import org.json.JSONPointer;

public class JsonPointerFormatValidator implements FormatValidator {

    @Override public Optional<String> validate(String subject) {
        if ("".equals(subject)) {
            return Optional.empty();
        }
        try {
            new JSONPointer(subject);
            if (subject.startsWith("#")) {
                return failure(subject);
            }
            return checkEscaping(subject);
        } catch (IllegalArgumentException e) {
            return failure(subject);
        }
    }

    protected Optional<String> failure(String subject) {
        return Optional.of(format("[%s] is not a valid JSON pointer", subject));
    }

    protected Optional<String> checkEscaping(String subject) {
        for (int i = 0; i < subject.length() - 1; ++i) {
            char c = subject.charAt(i);
            if (c == '~') {
                char next = subject.charAt(i + 1);
                if (next == '1' || next == '0') {
                    continue;
                }
                return failure(subject);
            }
        }
        if (subject.charAt(subject.length() - 1) == '~') {
            return failure(subject);
        }
        return Optional.empty();
    }

    @Override public String formatName() {
        return "json-pointer";
    }
}
