package org.everit.json.schema.listener;

import java.util.Objects;

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

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ValidationEvent<?> that = (ValidationEvent<?>) o;
        return schema.equals(that.schema) &&
                instance.equals(that.instance);
    }

    @Override public int hashCode() {
        return Objects.hash(schema, instance);
    }
}
