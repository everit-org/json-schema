package org.everit.json.schema.internal;

import org.everit.json.schema.Schema;
import org.json.JSONWriter;

import java.io.Writer;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class JSONPrinter {

    private final JSONWriter writer;

    public JSONPrinter(final Writer writer) {
        this(new JSONWriter(writer));
    }

    public JSONPrinter(final JSONWriter writer) {
        this.writer = requireNonNull(writer, "writer cannot be null");
    }

    public JSONPrinter key(final String key) {
        writer.key(key);
        return this;
    }

    public JSONPrinter value(final Object value) {
        writer.value(value);
        return this;
    }

    public JSONPrinter object() {
        writer.object();
        return this;
    }

    public JSONPrinter endObject() {
        writer.endObject();
        return this;
    }

    public JSONPrinter ifPresent(final String key, final Object value) {
        if (value != null) {
            key(key);
            value(value);
        }
        return this;
    }

    public JSONPrinter ifTrue(final String key, final Boolean value) {
        if (value != null && value) {
            key(key);
            value(value);
        }
        return this;
    }

    public JSONPrinter array() {
        writer.array();
        return this;
    }

    public JSONPrinter endArray() {
        writer.endArray();
        return this;
    }

    public void ifFalse(String key, Boolean value) {
        if (value != null && !value) {
            writer.key(key);
            writer.value(value);
        }
    }

    public <K> void printSchemaMap(Map<K, Schema> input) {
        object();

        for (Map.Entry<K, Schema> entry : input.entrySet()) {
            key(entry.getKey().toString());
            entry.getValue().describeTo(this);
        }

        endObject();
    }
}
