package org.everit.json.schema.listener;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

public class SubschemaReferencedEvent extends AbstractSchemaEvent {

    public SubschemaReferencedEvent(Schema schema, ValidationException rval) {
        super(schema, rval);
    }

}