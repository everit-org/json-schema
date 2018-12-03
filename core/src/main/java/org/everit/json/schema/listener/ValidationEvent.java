package org.everit.json.schema.listener;

import org.everit.json.schema.Schema;

public abstract class ValidationEvent<S extends Schema> {

    protected final S schema;

    protected final Object instance;

    protected ValidationEvent(S schema, Object instance) {
        this.schema = schema;
        this.instance = instance;
    }

    public S getSchema() {
        return schema;
    }

}
