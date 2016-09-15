package org.everit.json.schema.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.StringWriter;

import org.json.JSONObject;
import org.json.JSONWriter;
import org.junit.Before;
import org.junit.Test;

public class JSONPrinterTest {

  private StringWriter buffer;

  @Before
  public void before() {
    buffer = new StringWriter();
  }

  private JSONObject actualObj() {
    return new JSONObject(buffer.toString());
  }

  @Test
  public void constructor() {
    new JSONPrinter(new JSONWriter(new StringWriter()));
  }

  private JSONPrinter subject() {
    return new JSONPrinter(buffer);
  }

  @Test
  public void keyValueDelegates() {
    JSONPrinter subject = subject();
    subject.object();
    subject.key("mykey");
    subject.value("myvalue");
    subject.endObject();
    assertEquals("myvalue", actualObj().get("mykey"));
  }

  @Test
  public void ifPresentPrints() {
    JSONPrinter subject = subject();
    subject.object();
    subject.ifPresent("mykey", "myvalue");
    subject.endObject();
    assertEquals("myvalue", actualObj().get("mykey"));
  }

  @Test
  public void ifPresentOmits() {
    JSONPrinter subject = subject();
    subject.object();
    subject.ifPresent("mykey", null);
    subject.endObject();
    assertNull(actualObj().opt("mykey"));
  }

  @Test
  public void ifTruePints() {
    JSONPrinter subject = subject();
    subject.object();
    subject.ifTrue("uniqueItems", true);
    subject.endObject();
    assertEquals(true, actualObj().getBoolean("uniqueItems"));
  }

  @Test
  public void ifTrueHandlesNullAsFalse() {
    JSONPrinter subject = subject();
    subject.object();
    subject.ifTrue("uniqueItems", null);
    subject.endObject();
    assertNull(actualObj().opt("uniqueItems"));
  }

  @Test
  public void ifTrueOmits() {
    JSONPrinter subject = subject();
    subject.object();
    subject.ifTrue("uniqueItems", false);
    subject.endObject();
    assertNull(actualObj().opt("uniqueItems"));
  }

  @Test
  public void arraySupport() {
    JSONPrinter subject = subject();
    subject.array();
    subject.value(true);
    subject.endArray();
    assertEquals("[true]", buffer.toString());
  }

}
