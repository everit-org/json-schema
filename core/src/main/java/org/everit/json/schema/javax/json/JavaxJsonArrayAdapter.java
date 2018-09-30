package org.everit.json.schema.javax.json;

import org.everit.json.schema.spi.JsonArrayAdapter;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.List;

/**
 * A {@link JsonArrayAdapter} that delegates to a JSON-P {@link JsonArray}.
 */
class JavaxJsonArrayAdapter implements JsonArrayAdapter<JsonValue> {

    private final JsonArray delegate;

    JavaxJsonArrayAdapter(JsonArray delegate) {
        this.delegate = delegate;
    }

    @Override
    public int length() {
        return delegate.size();
    }

    @Override
    public JsonValue get(int index) {
        return delegate.get(index);
    }

    @Override
    public void put(int index, JsonValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<JsonValue> toList() {
        return Collections.unmodifiableList(delegate);
    }

    @Override
    public JsonValue unwrap() {
        return delegate;
    }

}
