package org.everit.json.schema.listener;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

public class SubschemaMismatchEvent extends ValidationEvent {

    public SubschemaMismatchEvent(Schema schema, ValidationException rval) {
        super(schema, rval);
    }

}
