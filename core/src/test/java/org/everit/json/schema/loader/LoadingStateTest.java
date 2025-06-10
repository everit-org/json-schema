package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.everit.json.schema.loader.JsonValueTest.withLs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.SchemaLocation;
import org.everit.json.schema.loader.internal.DefaultProviderValidators;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONPointer;
import org.junit.jupiter.api.Test;

/**
 * @author erosb
 */
public class LoadingStateTest {

    private static final LoaderConfig CONFIG = new LoaderConfig(new DefaultSchemaClient(),new DefaultProviderValidators(), SpecificationVersion.DRAFT_4, false);

    private LoadingState emptySubject() {
        return new LoadingState(CONFIG, emptyMap(), new HashMap<>(),
                new HashMap<>(), null, SchemaLocation.empty());
    }

    @Test
    public void childForString() {
        LoadingState ls = helloWorldObjState();
        LoadingState actual = ls.childFor("hello").ls;
        assertEquals(new SchemaLocation(asList("hello")), actual.pointerToCurrentObj);
    }

    @Test
    public void childForSecond() {
        LoadingState ls = helloWorldObjState();
        LoadingState actual = ls.childFor("hello").requireObject().childFor("world").ls;
        assertEquals(new SchemaLocation(asList("hello", "world")), actual.pointerToCurrentObj);
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
        assertEquals(new SchemaLocation(asList("0")), actual.pointerToCurrentObj);
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

    @Test
    public void testGetSubschemaRegistry() {
        JsonValue obj = JsonValue.of(ResourceLoader.DEFAULT.readObj("objecttestcases.json").getJSONObject("nestedIdV6"));
        Map<JsonValue, SubschemaRegistry> registries = new HashMap<>();
        LoadingState ls = new LoadingState(CONFIG, emptyMap(), obj, obj, null, SchemaLocation.empty(), registries);
        assertTrue(registries.isEmpty());
        SubschemaRegistry first = ls.getSubschemaRegistry(obj),
            second = ls.getSubschemaRegistry(obj);
        assertNotNull(first);
        assertSame(first, second);
    }

}
