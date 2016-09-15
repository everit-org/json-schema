package org.everit.json.schema.internal;

import static java.util.Objects.requireNonNull;

import java.io.Writer;

import org.json.JSONWriter;

public class JSONPrinter {

  private final JSONWriter writer;

  public JSONPrinter(final Writer writer) {
    this(new JSONWriter(writer));
  }

  public JSONPrinter(final JSONWriter writer) {
    this.writer = requireNonNull(writer, "writer cannot be null");
  }

  public void key(final String key) {
    writer.key(key);
  }

  public void value(final Object value) {
    writer.value(value);
  }

  public void object() {
    writer.object();
  }

  public void endObject() {
    writer.endObject();
  }

  public void ifPresent(final String key, final Object value) {
    if (value != null) {
      key(key);
      value(value);
    }
  }

  public void ifTrue(final String key, final Boolean value) {
    if (value != null && value) {
      key(key);
      value(value);
    }
  }

  public void array() {
    writer.array();
  }

  public void endArray() {
    writer.endArray();
  }

}
