package org.everit.json.schema.internal;

import static org.everit.json.schema.internal.TemporalFormatValidator.SECONDS_FRACTION_FORMATTER;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Optional;

import org.everit.json.schema.FormatValidator;

import com.google.common.collect.ImmutableList;

/**
 * Implementation of the "date-time" format value.
 */
public class DateTimeFormatValidator implements FormatValidator {

    private static class Delegate extends TemporalFormatValidator {

        Delegate() {
            super(FORMATTER, FORMATS_ACCEPTED);
        }

        @Override public String formatName() {
            return "date-time";
        }
    }

    private static final List<String> FORMATS_ACCEPTED = ImmutableList.of(
            "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z, yyyy-MM-dd'T'HH:mm:ss[+-]HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}[+-]HH:mm"
    );

    private static final String PARTIAL_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private static final DateTimeFormatter FORMATTER;

    static {
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern(PARTIAL_DATETIME_PATTERN)
                .appendOptional(SECONDS_FRACTION_FORMATTER)
                .appendPattern(TemporalFormatValidator.ZONE_OFFSET_PATTERN);

        FORMATTER = builder.toFormatter();
    }

    private Delegate delegate = new Delegate();

    @Override public Optional<String> validate(String subject) {
        return delegate.validate(subject);
    }

    @Override
    public String formatName() {
        return "date-time";
    }
}
