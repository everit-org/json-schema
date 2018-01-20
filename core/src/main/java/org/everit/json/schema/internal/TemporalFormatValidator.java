package org.everit.json.schema.internal;

import org.everit.json.schema.FormatValidator;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Base class for date and time format validators
 */
public class TemporalFormatValidator implements FormatValidator {
    final static DateTimeFormatter SECONDS_FRACTION_FORMATTER = new DateTimeFormatterBuilder()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .toFormatter();
    static final String ZONE_OFFSET_PATTERN = "XXX";

    private DateTimeFormatter formatter;
    private List<String> formatsAccepted;

    TemporalFormatValidator(DateTimeFormatter formatter, List<String> formatsAccepted) {
        this.formatter = requireNonNull(formatter, "formatter cannot be null");
        this.formatsAccepted = new ArrayList<>(formatsAccepted);
    }

    @Override
    public Optional<String> validate(final String subject) {
        try {
            formatter.parse(subject);
            return Optional.empty();
        } catch (DateTimeParseException e) {
            return Optional.of(String.format("[%s] is not a valid %s. Expected %s", subject, formatName(), formatsAccepted));
        }
    }
}
