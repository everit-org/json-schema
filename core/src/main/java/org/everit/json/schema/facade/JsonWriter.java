package org.everit.json.schema.facade;

import org.everit.json.schema.Schema;

import java.util.Map;

public interface JsonWriter {
    JsonWriter key(final String key);
    JsonWriter value(final Object value);
    JsonWriter object();
    JsonWriter endObject();
    JsonWriter array();
    JsonWriter endArray();

    default JsonWriter ifPresent(final String key, final Object value) {
        if (value != null) {
            key(key);
            value(value);
        }
        return this;
    }

    default JsonWriter ifTrue(final String key, final Boolean value) {
        if (value != null && value) {
            key(key);
            value(value);
        }
        return this;
    }

    default void ifFalse(String key, Boolean value) {
        if (value != null && !value) {
            key(key);
            value(value);
        }
    }

    default <K> void printSchemaMap(Map<K, Schema> input) {
        object();
        input.entrySet().forEach(entry -> {
            key(entry.getKey().toString());
            entry.getValue().describeTo(this);
        });
        endObject();
    }
}
