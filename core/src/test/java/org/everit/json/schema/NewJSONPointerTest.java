package org.everit.json.schema;

import org.everit.json.schema.JSONPointer;
import org.everit.json.schema.JSONPointerException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NewJSONPointerTest {

    @Test
    public void testEmptyPointer() {
        JSONPointer pointer = new JSONPointer("");
        JSONObject document = new JSONObject();
        document.put("key", "value");
        assertEquals(document, pointer.queryFrom(document));
    }

    @Test
    public void testSimplePointer() {
        JSONPointer pointer = new JSONPointer("/key");
        JSONObject document = new JSONObject();
        document.put("key", "value");
        assertEquals("value", pointer.queryFrom(document));
    }


    @Test
    public void testNestedObjectPointer() {
        JSONPointer pointer = new JSONPointer("/obj/key");
        JSONObject document = new JSONObject();
        JSONObject obj = new JSONObject();
        obj.put("key", "value");
        document.put("obj", obj);
        assertEquals("value", pointer.queryFrom(document));
    }

    @Test
    public void testArrayPointer() {
        JSONPointer pointer = new JSONPointer("/array/0");
        JSONObject document = new JSONObject();
        JSONArray array = new JSONArray();
        array.put("value");
        document.put("array", array);
        assertEquals("value", pointer.queryFrom(document));
    }

    @Test
    public void testInvalidPointer() {
        JSONPointer pointer = new JSONPointer("/key");
        JSONObject document = new JSONObject();
        document.put("other_key", "value");
        assertNull(pointer.queryFrom(document));
    }

    @Test(expected = JSONPointerException.class)
    public void testInvalidArrayIndexPointer() {
        JSONPointer pointer = new JSONPointer("/array/1");
        JSONObject document = new JSONObject();
        JSONArray array = new JSONArray();
        array.put("value");
        document.put("array", array);
        pointer.queryFrom(document);
    }
}
