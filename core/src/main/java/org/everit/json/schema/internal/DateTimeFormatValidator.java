package org.everit.json.schema.internal;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the "date-time" format value.
 */
public class DateTimeFormatValidator extends TemporalFormatValidator {

    private static final List<String> FORMATS_ACCEPTED = Arrays.asList(
            "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z, yyyy-MM-dd'T'HH:mm:ss[+-]HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}[+-]HH:mm"
    );

    private static final String PARTIAL_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern(PARTIAL_DATETIME_PATTERN)
            .appendOptional(SECONDS_FRACTION_FORMATTER)
            .appendPattern(TemporalFormatValidator.ZONE_OFFSET_PATTERN)
            .toFormatter();

    public DateTimeFormatValidator() {
        super(FORMATTER, FORMATS_ACCEPTED);
    }

    @Override
    public String formatName() {
        return "date-time";
    }
}
