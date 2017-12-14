package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.everit.json.schema.loader.JsonValueTest.withLs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.SchemaException;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONPointer;
import org.junit.Test;

/**
 * @author erosb
 */
public class LoadingStateTest {

    private LoadingState emptySubject() {
        LoaderConfig config = new LoaderConfig(new DefaultSchemaClient(), emptyMap(), SpecificationVersion.DRAFT_4, false);
        return new LoadingState(config, emptyMap(), new HashMap<>(),
                new HashMap<>(), null, emptyList());
    }

    @Test
    public void childForString() {
        LoadingState ls = helloWorldObjState();
        LoadingState actual = ls.childFor("hello").ls;
        assertEquals(asList("hello"), actual.pointerToCurrentObj);
    }

    @Test
    public void childForSecond() {
        LoadingState ls = helloWorldObjState();
        LoadingState actual = ls.childFor("hello").requireObject().childFor("world").ls;
        assertEquals(asList("hello", "world"), actual.pointerToCurrentObj);
    }

    protected LoadingState helloWorldObjState() {
        Map<String, Object> rawObj = new HashMap<>();
        Map<String, Object> worldObj = new HashMap<>();
        worldObj.put("world", true);
        rawObj.put("hello", worldObj);
        return withLs(JsonValue.of(rawObj)).ls;
    }

    @Test
    public void childForArrayIndex() {
        LoadingState subject = singleElemArrayState();
        LoadingState actual = subject.childFor(0).ls;
        assertEquals(asList("0"), actual.pointerToCurrentObj);
    }

    @Test
    public void testCreateSchemaException() {
        LoadingState subject = emptySubject();
        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#: message", actual.getMessage());
        assertEquals(JSONPointer.builder().build().toURIFragment(), actual.getSchemaLocation());
    }

    @Test
    public void testChildForFailure_NotFound() {
        LoadingState ls = helloWorldObjState();
        try {
            ls.childFor("nonexistent");
            fail("did not throw exception for nonexistent key");
        } catch (SchemaException e) {
            SchemaException expected = new SchemaException("#", "key [nonexistent] not found");
            assertEquals(expected, e);
        }
    }

    @Test
    public void testChildForArrayFailure_NotFound() {
        LoadingState subject = singleElemArrayState();
        try {
            subject.childFor(1);
            fail("did not throw exception");
        } catch (SchemaException e) {
            SchemaException expected = new SchemaException("#", "array index [1] is out of bounds");
            assertEquals(expected, e);
        }
    }

    @Test
    public void invalidArrayIndex() {
        try {
            singleElemArrayState().childFor("key");
            fail("did not throw exception");
        } catch (SchemaException e) {
            SchemaException expected = new SchemaException("#", "[key] is not an array index");
            assertEquals(expected, e);
        }
    }

    protected LoadingState singleElemArrayState() {
        return withLs(JsonValue.of(asList("elem"))).ls;
    }

}
