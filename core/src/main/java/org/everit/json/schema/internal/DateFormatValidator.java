package org.everit.json.schema.internal;

import com.google.common.collect.ImmutableList;

import java.time.format.DateTimeFormatter;

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
