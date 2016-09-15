package org.everit.json.schema.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.StringWriter;

import org.json.JSONObject;
import org.json.JSONWriter;
import org.junit.Test;

public class JSONPrinterTest {

  @Test
  public void constructor() {
    new JSONPrinter(new JSONWriter(new StringWriter()));
  }

  @Test
  public void keyValueDelegates() {
    StringWriter writer = new StringWriter();
    JSONPrinter subject = new JSONPrinter(writer);
    subject.object();
    subject.key("mykey");
    subject.value("myvalue");
    subject.endObject();
    String actual = writer.toString();
    assertEquals("{\"mykey\":\"myvalue\"}", actual);
  }

  @Test
  public void ifPresentPrints() {
    StringWriter writer = new StringWriter();
    JSONPrinter subject = new JSONPrinter(writer);
    subject.object();
    subject.ifPresent("mykey", "myvalue");
    subject.endObject();
    JSONObject actual = new JSONObject(writer.toString());
    assertEquals("myvalue", actual.get("mykey"));
  }

  @Test
  public void ifPresentOmits() {
    StringWriter writer = new StringWriter();
    JSONPrinter subject = new JSONPrinter(writer);
    subject.object();
    subject.ifPresent("mykey", null);
    subject.endObject();
    JSONObject actual = new JSONObject(writer.toString());
    assertNull(actual.opt("mykey"));
  }

}
