package org.everit.json.schema;

import org.everit.json.schema.spi.JsonAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class JSONAdaptationTest {

    private final JSONAdaptation adaptation = new JSONAdaptation();

    @Test
    public void testArrayType() {
        assertEquals(JSONArray.class, adaptation.arrayType());
    }

    @Test
    public void testObjectType() {
        assertEquals(JSONObject.class, adaptation.objectType());
    }

    @Test
    public void testIsSupportedType() {
        assertTrue(adaptation.isSupportedType(JSONArray.class));
        assertTrue(adaptation.isSupportedType(JSONObject.class));
        assertTrue(adaptation.isSupportedType(JSONObject.NULL.getClass()));
    }

    @Test
    public void testSupportedTypes() {
        final List<Class<?>> types = Arrays.asList(adaptation.supportedTypes());
        assertEquals(3, types.size());
        assertTrue(types.contains(JSONArray.class));
        assertTrue(types.contains(JSONObject.class));
        assertTrue(types.contains(JSONObject.NULL.getClass()));
    }

    @Test
    public void testAdaptIntrinsics() {
        assertEquals("value", adaptation.adapt("value"));
        assertEquals(true, adaptation.adapt(true));
        assertEquals(1, adaptation.adapt(1));
        assertNull(adaptation.adapt(null));
    }

    @Test
    public void testAdaptAdapter() {
        final JsonAdapter adapter = () -> null;
        assertSame(adapter, adaptation.adapt(adapter));
    }

    @Test
    public void testAdaptJSONNull() {
        assertNull(adaptation.adapt(JSONObject.NULL));
    }

    @Test
    public void testAdaptJSONObject() {
        final JSONObject object = new JSONObject();
        final Object result = adaptation.adapt(object);
        assertTrue(result instanceof JSONObjectAdapter);
        assertSame(object, ((JSONObjectAdapter) result).unwrap());
    }

    @Test
    public void testAdaptJSONArray() {
        final JSONArray array = new JSONArray();
        final Object result = adaptation.adapt(array);
        assertTrue(result instanceof JSONArrayAdapter);
        assertSame(array, ((JSONArrayAdapter) result).unwrap());
    }

    @Test
    public void testInvertIntrinsics() {
        assertEquals("value", adaptation.invert("value"));
        assertEquals(true, adaptation.invert(true));
        assertEquals(1, adaptation.invert(1));
        assertEquals(JSONObject.NULL, adaptation.adapt(null));
    }

    @Test
    public void testInvertObjectAdapter() {
        final JSONObject object = new JSONObject();
        assertSame(object, adaptation.invert(new JSONObjectAdapter(object)));
    }

    @Test
    public void testInvertArrayAdapter() {
        final JSONArray array = new JSONArray();
        assertSame(array, adaptation.invert(new JSONArrayAdapter(array)));
    }

}
