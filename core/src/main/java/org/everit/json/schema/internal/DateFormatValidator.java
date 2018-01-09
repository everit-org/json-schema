package org.everit.json.schema.internal;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import org.everit.json.schema.FormatValidator;

/**
 * Implementation of the "date" format value.
 */
public class DateFormatValidator extends TemporalFormatValidator {

    public DateFormatValidator() {
        super(DateTimeFormatter.ISO_LOCAL_DATE, ImmutableList.of("yyyy-MM-dd"));
    }

    @Override
    public String formatName() {
        return "date";
    }
}
