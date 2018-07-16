package org.everit.json.schema.internal;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the "time" format value.
 */
public class TimeFormatValidator extends TemporalFormatValidator {
    private static final String FORMATS_ACCEPTED = Arrays.asList(
            "HH:mm:ssZ",
            "HH:mm:ss.[0-9]{1,9}Z",
            "HH:mm:ss[+-]HH:mm",
            "HH:mm:ss.[0-9]{1,9}[+-]HH:mm"
    ).toString();

    private static final String PARTIAL_TIME_PATTERN = "HH:mm:ss";

    private static final DateTimeFormatter FORMATTER;

    static {
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern(PARTIAL_TIME_PATTERN)
                .appendOptional(SECONDS_FRACTION_FORMATTER)
                .appendPattern(ZONE_OFFSET_PATTERN);

        FORMATTER = builder.toFormatter();
    }

    public TimeFormatValidator() {
        super(FORMATTER, FORMATS_ACCEPTED);
    }

    @Override
    public String formatName() {
        return "time";
    }
}
