package org.everit.json.schema;

import org.everit.json.schema.spi.JsonObjectAdapter;
import org.json.JSONObject;

import java.util.Map;

/**
 * A {@link JsonObjectAdapter} that delegates to a {@link JSONObject}.
 */
class JSONObjectAdapter implements JsonObjectAdapter<Object> {

    private final JSONObject delegate;

    JSONObjectAdapter(JSONObject delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object unwrap() {
        return delegate;
    }

    @Override
    public int length() {
        return delegate.length();
    }

    @Override
    public String[] keys() {
        return JSONObject.getNames(delegate);
    }

    @Override
    public boolean has(String key) {
        return delegate.has(key);
    }

    @Override
    public Object get(String key) {
        return delegate.get(key);
    }

    @Override
    public void put(String key, Object value) {
        delegate.put(key, value);
    }

    @Override
    public Map<String, Object> toMap() {
        return delegate.toMap();
    }

}
