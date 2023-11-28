package org.everit.json.schema.internal;

import org.everit.json.schema.FormatValidator;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Optional;

import static org.everit.json.schema.internal.TemporalFormatValidator.SECONDS_FRACTION_FORMATTER;

/**
 * Implementation of the "date-time" format value.
 */
public class DateTimeFormatValidator extends TemporalFormatValidator {

    private static class Delegate extends TemporalFormatValidator {

        Delegate() {
            super(FORMATTER, FORMATS_ACCEPTED);
        }

        @Override
        public String formatName() {
            return "date-time";
        }
    }

    private static final String FORMATS_ACCEPTED = "['yyyy-MM-dd'T'HH:mm:ssZ', 'yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z', 'yyyy-MM-dd'T'HH:mm:ss[+-]HH:mm', 'yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}[+-]HH:mm']";

    private static final String PARTIAL_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern(PARTIAL_DATETIME_PATTERN)
            .appendOptional(SECONDS_FRACTION_FORMATTER)
            .optionalStart()
            .appendPattern(TemporalFormatValidator.ZONE_OFFSET_PATTERN)
            .optionalEnd()
            .toFormatter();


    private Delegate delegate = new Delegate();

    public DateTimeFormatValidator() {
        super(FORMATTER, FORMATS_ACCEPTED);
    }

    @Override
    public Optional<String> validate(String subject) {
        return delegate.validate(subject);
    }

    @Override
    public String formatName() {
        return delegate.formatName();
    }
}
