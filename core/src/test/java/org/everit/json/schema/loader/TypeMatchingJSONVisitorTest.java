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

    private static final LoadingState emptyLs = JsonValueTest.emptyLs;
    public static final JsonValue FLS = new JsonValue(false, emptyLs);
    public static final JsonValue TRU = new JsonValue(true, emptyLs);
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
        assertEquals("string", JSONVisitor.requireString(new JsonValue("string", emptyLs)));
    }

    @Test
    public void requireStringWithMapper() {
        Integer actual = JSONVisitor.requireString(new JsonValue("42", emptyLs), (e, ls) -> Integer.valueOf(e));
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
                new JsonValue(new JSONArray("[true,false]"), emptyLs)
        ));
    }

    @Test
    public void requireArrayWithMapper() {
        JsonValue input = new JsonValue(new JSONArray("[\"1\", \"2\"]"), emptyLs);
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
        JsonValue input = new JsonValue(new JSONArray("[\"1\", true]"), emptyLs);
        JSONVisitor.requireArray(input, (arr, ls) -> arr.stream().map(JSONVisitor::requireString)
                .map(Integer::valueOf)
                .map(i -> i.intValue() + 1)
                .collect(toList()));
    }

    @Test
    public void requireBooleanFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: Boolean, found: String");
        JSONVisitor.requireBoolean(new JsonValue("string", emptyLs));
    }

    @Test
    public void requireBooleanSuccess() {
        assertTrue(JSONVisitor.requireBoolean(new JsonValue(true, emptyLs)));
    }

    @Test
    public void requireBooleanWithMapper() {
        assertTrue(JSONVisitor.requireBoolean(new JsonValue(false, emptyLs), (bool, ls) -> !bool));
    }

    @Test
    public void requireObjectFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: Map, found: String");
        JSONVisitor.requireObject(new JsonValue(STR, emptyLs));
    }

    @Test
    public void requireObjecSuccess() {
        Map<String, JsonValue> actual = JSONVisitor.requireObject(new JsonValue(new JSONObject(), emptyLs));
        assertEquals(emptyMap(), actual);
    }

    @Test
    public void requireObjectWithMapping() {
        String actual = JSONVisitor.requireObject(new JsonValue(new JSONObject(), emptyLs), (obj, ls) -> "hello");
        assertEquals("hello", actual);
    }

}
