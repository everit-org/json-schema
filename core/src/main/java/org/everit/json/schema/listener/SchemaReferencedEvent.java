package org.everit.json.schema.listener;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

public class SchemaReferencedEvent extends AbstractSchemaEvent {

    public SchemaReferencedEvent(Schema schema, ValidationException rval) {
        super(schema, rval);
    }

}