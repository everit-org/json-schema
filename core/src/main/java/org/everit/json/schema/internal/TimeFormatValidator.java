package org.everit.json.schema.internal;

import com.google.common.collect.ImmutableList;
import org.everit.json.schema.FormatValidator;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;

/**
 * @author zgyorffi
 */
public class TimeFormatValidator implements FormatValidator {
    private static final List<String> FORMATS_ACCEPTED = ImmutableList.of(
            "HH:mm:ssZ", "HH:mm:ss.[0-9]{1,9}Z, HH:mm:ss[+-]HH:mm, HH:mm:ss.[0-9]{1,9}[+-]HH:mm"
    );

    private static final String PARTIAL_TIME_PATTERN = "HH:mm:ss";

    private static final String ZONE_OFFSET_PATTERN = "XXX";

    private static final DateTimeFormatter FORMATTER;

    static {
        final DateTimeFormatter secondsFractionFormatter = new DateTimeFormatterBuilder()
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                .toFormatter();

        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern(PARTIAL_TIME_PATTERN)
                .appendOptional(secondsFractionFormatter)
                .appendPattern(ZONE_OFFSET_PATTERN);

        FORMATTER = builder.toFormatter();
    }

    @Override
    public Optional<String> validate(final String subject) {
        try {
            FORMATTER.parse(subject);
            return Optional.empty();
        } catch (DateTimeParseException e) {
            return Optional.of(String.format("[%s] is not a valid time. Expected %s", subject, FORMATS_ACCEPTED));
        }
    }

    @Override
    public String formatName() {
        return "time";
    }
}
