package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.everit.json.schema.loader.JsonValueTest.withLs;
import static org.junit.Assert.assertEquals;

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
        LoaderConfig config = new LoaderConfig(new DefaultSchemaClient(), emptyMap(), SpecificationVersion.DRAFT_4);
        return new LoadingState(config, emptyMap(), new HashMap<>(),
                new HashMap<>(), null, emptyList());
    }

    @Test
    public void childForString() {
        LoadingState ls = emptySubject();
        LoadingState actual = ls.childFor("hello").ls;
        assertEquals(asList("hello"), actual.pointerToCurrentObj);
    }

    @Test
    public void childForSecond() {
        Map<String, Object> rawObj = new HashMap<>();
        Map<String, Object> worldObj = new HashMap<>();
        worldObj.put("world", true);
        rawObj.put("hello", worldObj);
        LoadingState ls = withLs(JsonValue.of(rawObj)).ls;
        LoadingState actual = ls.childFor("hello").requireObject().childFor("world").ls;
        assertEquals(asList("hello", "world"), actual.pointerToCurrentObj);
    }

    @Test
    public void childForArrayIndex() {
        LoadingState ls = emptySubject();
        LoadingState actual = ls.childFor(42).ls;
        assertEquals(asList("42"), actual.pointerToCurrentObj);
    }

    @Test
    public void testCreateSchemaException() {
        LoadingState subject = emptySubject();
        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#: message", actual.getMessage());
        assertEquals(JSONPointer.builder().build().toURIFragment(), actual.getSchemaLocation());
    }

}
