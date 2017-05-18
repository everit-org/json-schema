package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author erosb
 */
public class LoadingStateTest {

    private LoadingState emptySubject() {
        return new LoadingState(SchemaLoader.builder()
                .rootSchemaJson(new JSONObject())
                .schemaJson(new JSONObject()));
    }

    @Test
    public void childForString() {
        LoadingState ls = emptySubject();
        LoadingState actual = ls.childFor("hello");
        assertEquals(asList("hello"), actual.pointerToCurrentObj);
    }

    @Test
    public void childForSecond() {
        LoadingState ls = emptySubject();
        LoadingState actual = ls.childFor("hello").childFor("world");
        assertEquals(asList("hello", "world"), actual.pointerToCurrentObj);
    }

    @Test
    public void childForArrayIndex() {
        LoadingState ls = emptySubject();
        LoadingState actual = ls.childFor(42);
        assertEquals(asList("42"), actual.pointerToCurrentObj);
    }

    @Test
    public void testCreateSchemaException() {
        LoadingState subject = new LoadingState(SchemaLoader.builder().schemaJson(new JSONObject()));
        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#: message", actual.getMessage());
        assertEquals(JSONPointer.builder().build().toURIFragment(), actual.getSchemaLocation());
    }

    @Test
    public void childForNotnullId() {
        LoadingState actual = emptySubject().childForId("http://x.y");
        assertEquals("http://x.y", actual.id.toString());
    }

    @Test
    public void childForNullId() {
        LoadingState actual = emptySubject().childForId(null);
        assertNull(actual.id);
    }
}
