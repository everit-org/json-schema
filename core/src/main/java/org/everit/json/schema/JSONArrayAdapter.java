package org.everit.json.schema;

import org.everit.json.schema.spi.JsonArrayAdapter;
import org.json.JSONArray;

import java.util.List;

/**
 * A {@link JsonArrayAdapter} that delegates to a {@link JSONArray}.
 */
class JSONArrayAdapter implements JsonArrayAdapter<Object> {

    private final JSONArray delegate;

    JSONArrayAdapter(JSONArray delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object unwrap() {
        return delegate;
    }

    public Object get(int index) {
        return delegate.get(index);
    }

    public int length() {
        return delegate.length();
    }

    @Override
    public void put(int index, Object value) {
        delegate.put(index, value);
    }

    @Override
    public List<Object> toList() {
        return delegate.toList();
    }

}
