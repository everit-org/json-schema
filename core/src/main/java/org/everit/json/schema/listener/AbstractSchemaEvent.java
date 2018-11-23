package org.everit.json.schema.listener;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

public abstract class AbstractSchemaEvent {

    protected final Schema schema;
    protected final ValidationException rval;

    AbstractSchemaEvent(Schema schema, ValidationException rval) {
        this.schema = schema;
        this.rval = rval;
    }

    public Schema getSchema() {
        return schema;
    }

    public ValidationException getValidationException() {
        return rval;
    }

}
