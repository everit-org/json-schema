package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author erosb
 */
public class JSONTraverserTest {

    private final LoadingState emptyLs = new LoadingState(SchemaLoader.builder()
            .rootSchemaJson(new JSONObject())
            .schemaJson(new JSONObject()));

    @Rule
    public ExpectedException exc  = ExpectedException.none();

    @Test
    public void testBoolean() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONTraverser subject = new JSONTraverser(true, emptyLs);
        subject.accept(visitor);
        verify(visitor).visitBoolean(eq(true), notNull(LoadingState.class));
    }

    @Test
    public void boolArray() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONArray array = new JSONArray("[true]");
        JSONTraverser subject = new JSONTraverser(array, emptyLs);
        subject.accept(visitor);
        verify(visitor).visitArray(argThat(listOf(new JSONTraverser(true, emptyLs))), notNull(LoadingState.class));
    }

    @Test
    public void testString() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONTraverser subject = new JSONTraverser("string", emptyLs);
        subject.accept(visitor);
        verify(visitor).visitString(eq("string"), notNull(LoadingState.class));
    }

    @Test
    public void testObject() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONTraverser subject = new JSONTraverser(new JSONObject("{\"a\":true}"), emptyLs);
        subject.accept(visitor);
        HashMap<String, JSONTraverser> expected = new HashMap<>();
        expected.put("a", new JSONTraverser(true, emptyLs));
        verify(visitor).visitObject(eq(expected), notNull(LoadingState.class));
    }

    @Test
    public void emptyObj() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONTraverser subject = new JSONTraverser(new JSONObject("{}"), emptyLs);
        subject.accept(visitor);
        verify(visitor).visitObject(eq(emptyMap()), notNull(LoadingState.class));
    }

    private Matcher<List<JSONTraverser>> listOf(JSONTraverser... expected) {
        return new TypeSafeMatcher<List<JSONTraverser>>() {

            @Override protected boolean matchesSafely(List<JSONTraverser> item) {
                return new ArrayList<>(item).equals(new ArrayList<>(asList(expected)));
            }

            @Override public void describeTo(Description description) {

            }
        };
    }

    @Test
    public void ptrChangeOnArray() {
        JSONVisitor subject = new BaseJSONVisitor<Void>() {

            @Override public Void visitBoolean(boolean value, LoadingState ls) {
                if (value) {
                    assertEquals(asList("0"), ls.pointerToCurrentObj);
                } else {
                    assertEquals(asList("1"), ls.pointerToCurrentObj);
                }
                return null;
            }

        };
        new JSONTraverser(new JSONArray("[true,false]"), emptyLs).accept(subject);
    }

    @Test
    public void ptrChangeOnObject() {
        JSONVisitor subject = new BaseJSONVisitor<Void>() {

            @Override public Void visitBoolean(boolean value, LoadingState ls) {
                if (value) {
                    assertEquals(asList("a"), ls.pointerToCurrentObj);
                } else {
                    assertEquals(asList("b"), ls.pointerToCurrentObj);
                }
                return null;
            }

        };
        new JSONTraverser(new JSONObject("{\"a\":true,\"b\":false}"), emptyLs).accept(subject);
    }

    @Test @Ignore
    public void requireString() {
        exc.expect(SchemaException.class);
        JSONVisitor.requireString(new JSONTraverser(true, emptyLs));
    }

}
