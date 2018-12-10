package org.everit.json.schema.event;

import java.util.Objects;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CombinedSchemaMismatchEvent extends CombinedSchemaValidationEvent implements MismatchEvent {

    private final ValidationException failure;

    public CombinedSchemaMismatchEvent(CombinedSchema schema, Schema subSchema, Object instance, ValidationException failure) {
        super(schema, subSchema, instance);
        this.failure = failure;
    }

    @Override public ValidationException getFailure() {
        return failure;
    }

    @Override void describeTo(JSONObject obj) {
        obj.put("type", "mismatch");
        obj.put("keyword", schema.getCriterion().toString());
        obj.put("subSchema", new JSONTokener(subSchema.toString()).nextValue());
        obj.put("failure", failure.toJSON());
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CombinedSchemaMismatchEvent))
            return false;
        if (!super.equals(o))
            return false;
        CombinedSchemaMismatchEvent that = (CombinedSchemaMismatchEvent) o;
        return failure.equals(that.failure);
    }

    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), failure);
    }

    @Override boolean canEqual(Object o) {
        return o instanceof CombinedSchemaMismatchEvent;
    }
}
