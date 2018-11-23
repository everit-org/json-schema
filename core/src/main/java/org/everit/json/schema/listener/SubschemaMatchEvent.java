package org.everit.json.schema.listener;

import org.everit.json.schema.Schema;

public class SubschemaMatchEvent extends AbstractSchemaEvent {

    public SubschemaMatchEvent(Schema schema) {
        super(schema, null);
    }

}