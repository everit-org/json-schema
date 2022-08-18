package org.everit.json.schema.internal;

import org.everit.json.schema.NullSchema;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONPrinterTest {

    private StringWriter buffer;

    @BeforeEach
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
    public void ifFalsePrints() {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifFalse("mykey", false);
        subject.endObject();
        assertEquals(false, actualObj().getBoolean("mykey"));
    }

    @Test
    public void ifFalseOmits() {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifFalse("mykey", true);
        subject.endObject();
        assertNull(actualObj().opt("mykey"));
    }

    @Test
    public void ifFalseHandlesNullAsTrue() {
        JSONPrinter subject = subject();
        subject.object();
        subject.ifFalse("mykey", null);
        subject.endObject();
        assertNull(actualObj().opt("mykey"));
    }

    @Test
    public void arraySupport() {
        JSONPrinter subject = subject();
        subject.array();
        subject.value(true);
        subject.endArray();
        assertEquals("[true]", buffer.toString());
    }

    @Test
    public void printSchemaMap() {
        Map<Number, Schema> input = new LinkedHashMap<>();
        input.put(2, NullSchema.INSTANCE);
        subject().printSchemaMap(input);
        assertEquals("{\"2\":" + NullSchema.INSTANCE + "}", buffer.toString());
    }

}
