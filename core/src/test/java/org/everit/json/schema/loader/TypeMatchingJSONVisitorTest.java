package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author erosb
 */
public class TypeMatchingJSONVisitorTest {

    private static final LoadingState emptyLs = JSONValueTest.emptyLs;
    public static final JSONValue FLS = new JSONValue(false, emptyLs);
    public static final JSONValue TRU = new JSONValue(true, emptyLs);
    public static final String STR = "string";

    @Rule
    public ExpectedException exc = ExpectedException.none();

    @Test
    public void requireStringFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: String, found: Boolean");
        JSONVisitor.requireString(TRU);
    }

    @Test
    public void requireStringSuccess() {
        assertEquals("string", JSONVisitor.requireString(new JSONValue("string", emptyLs)));
    }

    @Test
    public void requireStringWithMapper() {
        Integer actual = JSONVisitor.requireString(new JSONValue("42", emptyLs), (e, ls) -> Integer.valueOf(e));
        assertEquals(Integer.valueOf(42), actual);
    }

    @Test
    public void requireArrayFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: List, found: Boolean");
        JSONVisitor.requireArray(TRU);
    }

    @Test
    public void requireArraySuccess() {
        assertEquals(asList(TRU, FLS), JSONVisitor.requireArray(
                new JSONValue(new JSONArray("[true,false]"), emptyLs)
        ));
    }

    @Test
    public void requireArrayWithMapper() {
        JSONValue input = new JSONValue(new JSONArray("[\"1\", \"2\"]"), emptyLs);
        assertEquals(asList(2, 3), JSONVisitor.requireArray(input,
                (arr, ls) -> arr.stream().map(JSONVisitor::requireString)
                        .map(Integer::valueOf)
                        .map(i -> i.intValue() + 1)
                        .collect(toList())));
    }

    @Test
    public void requireArrayFailureInside() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#/1: expected type: String, found: Boolean");
        JSONValue input = new JSONValue(new JSONArray("[\"1\", true]"), emptyLs);
        JSONVisitor.requireArray(input, (arr, ls) -> arr.stream().map(JSONVisitor::requireString)
                .map(Integer::valueOf)
                .map(i -> i.intValue() + 1)
                .collect(toList()));
    }

    @Test
    public void requireBooleanFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: Boolean, found: String");
        JSONVisitor.requireBoolean(new JSONValue("string", emptyLs));
    }

    @Test
    public void requireBooleanSuccess() {
        assertTrue(JSONVisitor.requireBoolean(new JSONValue(true, emptyLs)));
    }

    @Test
    public void requireBooleanWithMapper() {
        assertTrue(JSONVisitor.requireBoolean(new JSONValue(false, emptyLs), (bool, ls) -> !bool));
    }

    @Test
    public void requireObjectFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: Map, found: String");
        JSONVisitor.requireObject(new JSONValue(STR, emptyLs));
    }

    @Test
    public void requireObjecSuccess() {
        Map<String, JSONValue> actual = JSONVisitor.requireObject(new JSONValue(new JSONObject(), emptyLs));
        assertEquals(emptyMap(), actual);
    }

    @Test
    public void requireObjectWithMapping() {
        String actual = JSONVisitor.requireObject(new JSONValue(new JSONObject(), emptyLs), (obj, ls) -> "hello");
        assertEquals("hello", actual);
    }

}
