package org.everit.json.schema.internal;

import org.everit.json.schema.FormatValidator;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Implementation of the `duration` format value.
 */
public class DurationFormatValidator implements FormatValidator {
    @Override
    public Optional<String> validate(String subject) {
        try {
            Duration.parse(subject);
            return Optional.empty();
        } catch (DateTimeParseException ex) {
            return Optional.of(String.format("[%s] is not a valid %s. Expected ISO-8601 duration format", subject, formatName()));
        }
    }

    @Override
    public String formatName() {
        return "duration";
    }
}
