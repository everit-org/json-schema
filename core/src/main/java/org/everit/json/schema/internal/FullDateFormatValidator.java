package org.everit.json.schema.internal;

/**
 * Implementation of the "full-date" format value.
 */
public class FullDateFormatValidator extends DateTimeSpecFormatValidator {

    private static final String[] FULLDATE_FORMAT_STRINGS = {"yyyy-MM-dd"};

    @Override
    public String formatName() {
        return "full-date";
    }

    @Override
    protected String[] getValidDatetimeFormats() {
        return FULLDATE_FORMAT_STRINGS;
    }
}
