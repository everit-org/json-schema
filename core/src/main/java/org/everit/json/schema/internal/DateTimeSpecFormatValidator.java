package org.everit.json.schema.internal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.function.Predicate;
import org.everit.json.schema.FormatValidator;

public abstract class DateTimeSpecFormatValidator implements FormatValidator {

    abstract String[] getValidDatetimeFormats();

    private SimpleDateFormat dateFormat(final String pattern) {
        SimpleDateFormat rval = new SimpleDateFormat(pattern);
        rval.setLenient(false);
        return rval;
    }

    @Override
    public Optional<String> validate(final String subject) {

        Predicate<String> isValidFormat = isValidFormatFor(subject);

        if (Arrays.stream(getValidDatetimeFormats())
                .anyMatch(isValidFormat)) {
            return Optional.empty();
        } else {
            return Optional.of(String.format("[%s] is not a valid " + formatName(), subject));
        }
    }

    private Predicate<String> isValidFormatFor(String subject) {
        return format -> {
            ParsePosition pos = new ParsePosition(0);
            dateFormat(format).parse(subject, pos);

            //Parse can be forgiving to extraneous characters - check the full string was parsed with no error
            return pos.getErrorIndex() < 0 && pos.getIndex() == subject.length();
        };
    }
}
