package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author erosb
 */
public class JsonValueTest {

    public static final String FINISH = "finish";

    private static class DummyJSONVisitor implements JSONVisitor<String> {

        @Override public String visitBoolean(Boolean value, LoadingState ls) {
            return "boolean";
        }

        @Override public String visitArray(List<JsonValue> value, LoadingState ls) {
            return "array";
        }

        @Override public String visitString(String value, LoadingState ls) {
            return "string";
        }

        @Override public String visitInteger(Integer value, LoadingState ls) {
            return "integer";
        }

        @Override public String visitObject(Map<String, JsonValue> obj, LoadingState ls) {
            return "object";
        }

        @Override public String visitNull(LoadingState ls) {
            return "null";
        }

        @Override public String finishedVisiting(LoadingState ls) {
            return null;
        }
    }

    private DummyJSONVisitor dummyVisitor = new DummyJSONVisitor();

    static final LoadingState emptyLs = new LoadingState(SchemaLoader.builder()
            .rootSchemaJson(new JSONObject())
            .schemaJson(new JSONObject()));
    public static final JsonValue OBJ = new JsonObject(emptyMap(), emptyLs);

    public static final JsonValue FLS = new JsonValue(false, emptyLs);


    public static final JsonValue TRU = new JsonValue(true, emptyLs);

    public static final JsonValue STR = new JsonValue("string", emptyLs);

    @Rule
    public ExpectedException exc = ExpectedException.none();

    @Test
    public void testBoolean() {
        JSONVisitor<String> visitor = spy(dummyVisitor);
        JsonValue subject = new JsonValue(true, emptyLs);
        String actual = subject.accept(visitor);
        verify(visitor).visitBoolean(eq(true), notNull(LoadingState.class));
        verify(visitor).finishedVisiting(emptyLs);
        assertEquals("boolean", actual);
    }

    @Test
    public void boolArray() {
        dummyVisitor = new DummyJSONVisitor();
        JSONVisitor<String> visitor = spy(dummyVisitor);
        JSONArray array = new JSONArray("[true]");
        JsonValue subject = new JsonValue(array, emptyLs);
        String actual = subject.accept(visitor);
        verify(visitor).visitArray(argThat(listOf(new JsonValue(true, emptyLs))), notNull(LoadingState.class));
        verify(visitor).finishedVisiting(emptyLs);
        assertEquals("array", actual);
    }

    @Test
    public void testString() {
        JSONVisitor<String> visitor = spy(dummyVisitor);
        JsonValue subject = new JsonValue("string", emptyLs);
        String actual = subject.accept(visitor);
        verify(visitor).visitString(eq("string"), notNull(LoadingState.class));
        verify(visitor).finishedVisiting(emptyLs);
        assertEquals("string", actual);
    }

    @Test
    public void testJSONNullHandling() {
        JSONVisitor<String> visitor = spy(dummyVisitor);
        JsonValue subject = new JsonValue(JSONObject.NULL, emptyLs);
        String actual = subject.accept(visitor);
        verify(visitor).visitNull(emptyLs);
        verify(visitor).finishedVisiting(emptyLs);
        assertEquals("null", actual);
    }

    @Test
    public void testNullReferenceHandling() {
        JSONVisitor<String> visitor = spy(dummyVisitor);
        JsonValue subject = new JsonValue(null, emptyLs);
        String actual = subject.accept(visitor);
        verify(visitor).visitNull(emptyLs);
        verify(visitor).finishedVisiting(emptyLs);
        assertEquals("null", actual);
    }

    @Test
    public void testObject() {
        JSONVisitor<String> visitor = spy(dummyVisitor);
        JsonValue subject = new JsonValue(new JSONObject("{\"a\":true}"), emptyLs);
        String actual = subject.accept(visitor);
        HashMap<String, JsonValue> expected = new HashMap<>();
        expected.put("a", new JsonValue(true, emptyLs));
        verify(visitor).visitObject(eq(expected), notNull(LoadingState.class));
        verify(visitor).finishedVisiting(emptyLs);
        assertEquals("object", actual);
    }

    @Test
    public void emptyObj() {
        JSONVisitor<String> visitor = spy(dummyVisitor);
        JsonValue subject = new JsonValue(new JSONObject("{}"), emptyLs);
        String actual = subject.accept(visitor);
        verify(visitor).visitObject(eq(emptyMap()), notNull(LoadingState.class));
        verify(visitor).finishedVisiting(emptyLs);
        assertEquals("object", actual);
    }

    private Matcher<List<JsonValue>> listOf(JsonValue... expected) {
        return new TypeSafeMatcher<List<JsonValue>>() {

            @Override protected boolean matchesSafely(List<JsonValue> item) {
                return new ArrayList<>(item).equals(new ArrayList<>(asList(expected)));
            }

            @Override public void describeTo(Description description) {

            }
        };
    }

    @Test
    public void ptrChangeOnArray() {
        JSONVisitor subject = new BaseJSONVisitor<Void>() {

            @Override public Void visitBoolean(Boolean value, LoadingState ls) {
                if (value) {
                    assertEquals(asList("0"), ls.pointerToCurrentObj);
                } else {
                    assertEquals(asList("1"), ls.pointerToCurrentObj);
                }
                return null;
            }

        };
        new JsonValue(new JSONArray("[true,false]"), emptyLs).accept(subject);
    }

    @Test
    public void ptrChangeOnObject() {
        JSONVisitor<String> subject = new BaseJSONVisitor<String>() {

            @Override public String visitBoolean(Boolean value, LoadingState ls) {
                if (value) {
                    assertEquals(asList("a"), ls.pointerToCurrentObj);
                } else {
                    assertEquals(asList("b"), ls.pointerToCurrentObj);
                }
                return null;
            }

        };
        new JsonValue(new JSONObject("{\"a\":true,\"b\":false}"), emptyLs).accept(subject);
    }

    @Test
    public void finisherOverridesRetval() {
        JSONVisitor<String> visitor = new DummyJSONVisitor() {

            @Override public String finishedVisiting(LoadingState ls) {
                return FINISH;
            }
        };
        String actual = new JsonValue(true, emptyLs).accept(visitor);
        assertEquals(FINISH, actual);
    }

    @Test
    public void requireStringFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: String, found: Boolean");
        TRU.requireString();
    }

    @Test
    public void requireStringSuccess() {
        assertEquals("string", STR.requireString());
    }

    @Test
    public void requireStringWithMapper() {
        Integer actual = new JsonValue("42", emptyLs).requireString((e, ls) -> Integer.valueOf(e));
        assertEquals(Integer.valueOf(42), actual);
    }

    @Test
    public void requireBooleanFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: Boolean, found: String");
        STR.requireBoolean();
    }

    @Test
    public void requireBooleanSuccess() {
        assertTrue(TRU.requireBoolean());
    }

    @Test
    public void requireBooleanWithMapper() {
        assertTrue(FLS.requireBoolean((bool, ls) -> !bool));
    }

    @Test
    public void requireObjectFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: JsonObject, found: String");
        STR.requireObject();
    }

    @Test
    public void requireObjectSuccess() {
        JsonObject actual = OBJ.requireObject();
        assertSame(OBJ, actual);
    }

    @Test
    public void requireObjectWithMapping() {
        String actual = OBJ.requireObject((obj, ls) -> "hello");
        assertEquals("hello", actual);
    }


}
