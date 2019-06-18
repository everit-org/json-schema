package org.everit.json.schema;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import org.everit.json.schema.internal.DateTimeFormatValidator;

public class CustomDateTimeFormatValidator extends DateTimeFormatValidator {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private DateFormat dateFormat;

    public CustomDateTimeFormatValidator() {
        dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        dateFormat.setLenient(false);
    }

    @Override
    public Optional<String> validate(String subject) {
        try {
            dateFormat.parse(subject);
            return Optional.empty();
        } catch (ParseException e) {
            return Optional.of(String.format("[%s] is not a valid %s. Expected %s", subject, formatName(), DATE_TIME_FORMAT));
        }

    }
}