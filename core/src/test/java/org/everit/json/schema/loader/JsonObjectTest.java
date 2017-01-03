package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author erosb
 */
public class JsonObjectTest {

    private Map<String, Object> storage() {
        Map<String, Object> rval = new HashMap<>();
        rval.put("a", true);
        rval.put("b", new JSONObject());
        return rval;
    }

    private final LoadingState emptyLs = JsonValueTest.emptyLs;

    @Rule
    public ExpectedException expExc = ExpectedException.none();

    @Test
    public void testHasKey() {
        assertTrue(subject().containsKey("a"));
    }

    private JsonObject subject() {
        return new JsonObject(storage(), JsonValueTest.emptyLs);
    }

    @SuppressWarnings("unchecked")
    private BiConsumer<JsonValue, LoadingState> mockConsumer() {
        return (BiConsumer<JsonValue, LoadingState>) mock(BiConsumer.class);
    }

    @Test
    public void testRequireWithConsumer() {
        BiConsumer<JsonValue, LoadingState> consumer = mockConsumer();
        subject().require("a", consumer);
        LoadingState lsForPath = emptyLs.childFor("a");
        verify(consumer).accept(JsonValue.of(true, lsForPath), lsForPath);
    }

    @Test
    public void testRequireWithConsumerFailure() {
        expExc.expect(SchemaException.class);
        expExc.expectMessage("#: required key [aaa] not found");
        BiConsumer<JsonValue, LoadingState> consumer = mockConsumer();
        subject().require("aaa", consumer);
        verify(consumer, never()).accept(any(), any());
    }

    @Test
    public void testRequireWithFunction() {
        BiFunction<JsonValue, LoadingState, Boolean> fn = (val, ls) -> false;
        assertFalse(subject().require("a", fn));
    }

    @Test
    public void testRequireWithFunctionFailure() {
        expExc.expect(SchemaException.class);
        expExc.expectMessage("#: required key [aaa] not found");
        subject().require("aaa", (val, ls) -> false);
    }

    @Test
    public void testMaybeWithConsumer() {
        BiConsumer<JsonValue, LoadingState> consumer = mockConsumer();
        subject().maybe("a", consumer);
        LoadingState lsForPath = emptyLs.childFor("a");
        verify(consumer).accept(JsonValue.of(true, lsForPath), lsForPath);
    }

    @Test
    public void testMaybeWithConsumerMiss() {
        BiConsumer<JsonValue, LoadingState> consumer = mockConsumer();
        subject().maybe("aaa", consumer);
        verify(consumer, never()).accept(any(), any());
    }

    @Test
    public void testMaybeWithFn() {
        assertEquals(Integer.valueOf(42), subject().maybe("a", (obj, ls) -> 42).get());
    }

    @Test
    public void testMaybeWithFnMiss() {
        assertEquals(Optional.empty(), subject().maybe("aaaa", (obj, ls) -> 42));
    }
}
