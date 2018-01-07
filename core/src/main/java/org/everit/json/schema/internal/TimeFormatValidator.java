package org.everit.json.schema.internal;

import com.google.common.collect.ImmutableList;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the "time" format value.
 */
public class TimeFormatValidator extends DateTimeFormatValidator {
    private static final List<String> FORMATS_ACCEPTED = ImmutableList.of(
            "HH:mm:ssZ", "HH:mm:ss.[0-9]{1,9}Z, HH:mm:ss[+-]HH:mm, HH:mm:ss.[0-9]{1,9}[+-]HH:mm"
    );

    private static final String PARTIAL_TIME_PATTERN = "HH:mm:ss";

    private static final DateTimeFormatter FORMATTER;

    static {
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern(PARTIAL_TIME_PATTERN)
                .appendOptional(SECONDS_FRACTION_FORMATTER)
                .appendPattern(ZONE_OFFSET_PATTERN);

        FORMATTER = builder.toFormatter();
    }

    @Override
    DateTimeFormatter formatter() {
        return FORMATTER;
    }

    @Override
    List<String> formats_accepted() {
        return FORMATS_ACCEPTED;
    }

    @Override
    public String formatName() {
        return "time";
    }
}
