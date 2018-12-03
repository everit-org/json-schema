package org.everit.json.schema.listener;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;

public class SchemaReferencedEvent extends ValidationEvent<ReferenceSchema> {

    private final Schema referredSchema;

    public SchemaReferencedEvent(ReferenceSchema schema, Object instance, Schema referredSchema) {
        super(schema, instance);
        this.referredSchema = referredSchema;
    }

    public Schema getReferredSchema() {
        return referredSchema;
    }
}
