package org.everit.json.schema.internal;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.everit.json.schema.FormatValidator;

public class RegexFormatValidator implements FormatValidator {

    @Override public Optional<String> validate(String subject) {
        try {
            Pattern.compile(subject);
        } catch (PatternSyntaxException e) {
            return Optional.of(String.format("[%s] is not a valid regular expression", subject));
        }
        return Optional.empty();
    }

    @Override public String formatName() {
        return "regex";
    }
}
