package org.everit.json.schema.internal;

/**
 * Implementation of the "date-time" format value.
 */
public class FullDateFormatValidator extends DateTimeSpecFormatValidator {

    private static final String[] DATETIME_FORMAT_STRINGS = {"yyyy-MM-dd"};

    @Override
    public String formatName() {
        return "full-date";
    }

    @Override
    protected String[] getValidDatetimeFormats() {
        return DATETIME_FORMAT_STRINGS;
    }
}
