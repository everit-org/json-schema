package org.everit.json.schema.internal;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.everit.json.schema.FormatValidator;

/**
 * Implementation of the "date" format value.
 */
public class DateFormatValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        try {
            DateTimeFormatter.ISO_LOCAL_DATE.parse(subject);
            return Optional.empty();
        } catch (DateTimeParseException e) {
            return Optional.of(String.format("[%s] is not a valid date. Expected [yyyy-MM-dd]", subject));
        }
    }

    @Override
    public String formatName() {
        return "date";
    }
}
