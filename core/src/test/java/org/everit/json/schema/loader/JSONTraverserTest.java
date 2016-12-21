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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author erosb
 */
public class JSONTraverserTest {

    @Rule
    public ExpectedException exc  = ExpectedException.none();

    @Test
    public void testBoolean() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONTraverser subject = new JSONTraverser(true);
        subject.accept(visitor);
        verify(visitor).visitBoolean(true);
    }

    @Test
    public void boolArray() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONArray array = new JSONArray("[true]");
        JSONTraverser subject = new JSONTraverser(array);
        subject.accept(visitor);
        verify(visitor).visitArray(argThat(listOf(new JSONTraverser(true))));
    }

    @Test
    public void testString() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONTraverser subject = new JSONTraverser("string");
        subject.accept(visitor);
        verify(visitor).visitString("string");
    }

    @Test
    public void testObject() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONTraverser subject = new JSONTraverser(new JSONObject("{\"a\":true}"));
        subject.accept(visitor);
        HashMap<String, JSONTraverser> expected = new HashMap<>();
        expected.put("a", new JSONTraverser(true));
        verify(visitor).visitObject(expected);
    }

    @Test
    public void emptyObj() {
        JSONVisitor visitor = mock(JSONVisitor.class, Mockito.CALLS_REAL_METHODS);
        JSONTraverser subject = new JSONTraverser(new JSONObject("{}"));
        subject.accept(visitor);
        verify(visitor).visitObject(emptyMap());
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

            @Override public Void visitBoolean(boolean value) {
                if (value) {
                    assertEquals("#/0", getCurrentPointer());
                } else {
                    assertEquals("#/1", getCurrentPointer());
                }
                return null;
            }

        };
        new JSONTraverser(new JSONArray("[true,false]")).accept(subject);
    }

    @Test
    public void ptrChangeOnObject() {
        JSONVisitor subject = new BaseJSONVisitor<Void>() {

            @Override public Void visitBoolean(boolean value) {
                if (value) {
                    assertEquals("#/a", getCurrentPointer());
                } else {
                    assertEquals("#/b", getCurrentPointer());
                }
                return null;
            }

        };
        new JSONTraverser(new JSONObject("{\"a\":true,\"b\":false}")).accept(subject);
    }

    @Test @Ignore
    public void requireString() {
        exc.expect(SchemaException.class);
        JSONVisitor.requireString(new JSONTraverser(true));
    }

}
