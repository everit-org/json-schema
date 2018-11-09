package org.everit.json.schema.internal;

import org.threeten.bp.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Implementation of the "date" format value.
 */
public class DateFormatValidator extends TemporalFormatValidator {

    public DateFormatValidator() {
        super(DateTimeFormatter.ISO_LOCAL_DATE, Collections.singletonList("yyyy-MM-dd").toString());
    }

    @Override
    public String formatName() {
        return "date";
    }
}
