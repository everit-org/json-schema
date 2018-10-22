package org.everit.json.schema.internal;

import org.everit.json.schema.Schema;
import org.everit.json.schema.facade.JsonWriter;
import org.json.JSONWriter;

import java.io.Writer;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Deprecated // TODO: Document: use new API, to be removed in the future
// TODO: Move to own maven artifact (with json org dependency)
public class JSONPrinter implements JsonWriter {
    private final JsonWriter writer;

    public JSONPrinter(final Writer writer) {
        this(new JSONWriter(writer));
    }

    public JSONPrinter(final JSONWriter writer) {
        this(new Wrapper(writer));
    }

    public JSONPrinter(final JsonWriter writer) {
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
        JsonWriter.super.ifPresent(key, value);
        return this;
    }

    public JSONPrinter ifTrue(final String key, final Boolean value) {
        JsonWriter.super.ifTrue(key, value);
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

    private static class Wrapper implements JsonWriter {
        private final JSONWriter writer;

        private Wrapper(JSONWriter writer) {
            this.writer = writer;
        }

        @Override
        public JsonWriter key(String key) {
            writer.key(key);
            return this;
        }

        @Override
        public JsonWriter value(Object value) {
            writer.value(value);
            return this;
        }

        @Override
        public JsonWriter object() {
            writer.object();
            return this;
        }

        @Override
        public JsonWriter endObject() {
            writer.endObject();
            return this;
        }

        @Override
        public JsonWriter array() {
            writer.array();
            return this;
        }

        @Override
        public JsonWriter endArray() {
            writer.endArray();
            return this;
        }
    }
}
