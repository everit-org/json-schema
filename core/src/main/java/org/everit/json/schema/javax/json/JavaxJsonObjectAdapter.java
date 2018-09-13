package org.everit.json.schema.javax.json;

import org.everit.json.schema.spi.JsonObjectAdapter;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.Map;

/**
 * A {@link JsonObjectAdapter} that delegates to a JSON-P {@link JsonObject}.
 */
class JavaxJsonObjectAdapter implements JsonObjectAdapter<JsonValue> {

    private final JsonObject delegate;

    JavaxJsonObjectAdapter(JsonObject delegate) {
        this.delegate = delegate;
    }

    @Override
    public int length() {
        return delegate.size();
    }

    @Override
    public String[] keys() {
        return delegate.keySet().toArray(new String[0]);
    }

    @Override
    public boolean has(String key) {
        return delegate.containsKey(key);
    }

    @Override
    public JsonValue get(String key) {
        return delegate.get(key);
    }

    @Override
    public void put(String key, JsonValue value) {
        delegate.put(key, value);
    }

    @Override
    public Map<String, JsonValue> toMap() {
        return Collections.unmodifiableMap(delegate);
    }

    @Override
    public JsonValue unwrap() {
        return delegate;
    }

}
