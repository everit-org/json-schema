package org.everit.json.schema.event;

import java.util.Objects;

import org.everit.json.schema.Schema;
import org.json.JSONObject;

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
        if (o == null)
            return false;
        if (!canEqual(o))
            return false;
        ValidationEvent<?> that = (ValidationEvent<?>) o;
        if (!that.canEqual(this)) {
            return false;
        }
        return schema.equals(that.schema) &&
                instance.equals(that.instance);
    }

    boolean canEqual(Object o) {
        return o instanceof ValidationEvent;
    }

    @Override public int hashCode() {
        return Objects.hash(schema, instance);
    }

    @Override
    public String toString() {
        return toJSON(false, false).toString();
    }

    public JSONObject toJSON(boolean includeSchema, boolean includeInstance) {
        JSONObject json = new JSONObject();
        if (includeSchema) {
            json.put("schema", new JSONObject(schema.toString()));
        }
        if (includeInstance) {
            json.put("instance", instance);
        }
        describeTo(json);
        return json;
    }

    abstract void describeTo(JSONObject obj);
}
