package org.everit.json.schema.listener;

import java.util.Objects;

import org.everit.json.schema.ConditionalSchema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

public class ConditionalSchemaMismatchEvent extends ConditionalSchemaValidationEvent implements MismatchEvent {

    private final ValidationException failure;

    protected ConditionalSchemaMismatchEvent(ConditionalSchema schema, Object instance, Keyword keyword, ValidationException failure) {
        super(schema, instance, keyword);
        this.failure = failure;
    }

    @Override public ValidationException getFailure() {
        return failure;
    }

    @Override void describeTo(JSONObject obj) {
        obj.put("type", "mismatch");
        obj.put("keyword", keyword.toString());
        obj.put("failure", failure.toJSON());
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ConditionalSchemaMismatchEvent))
            return false;
        if (!super.equals(o))
            return false;
        ConditionalSchemaMismatchEvent that = (ConditionalSchemaMismatchEvent) o;
        return failure.equals(that.failure);
    }

    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), failure);
    }
}
