package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.everit.json.schema.SchemaException;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ProjectedJsonObjectTest {

    private JsonObject original = new JsonObject(ImmutableMap.<String, Object>builder()
            .put("minimum", 2)
            .put("maximum", 20)
            .put("not", ImmutableMap.<String, Object>builder()
                    .put("multipleOf", 3)
                    .put("maximum", 5)
                    .build())
            .build());

    {
        original.ls = new LoadingState(LoaderConfig.defaultV4Config(), emptyMap(), original, original, null, emptyList());
    }

    private ProjectedJsonObject createSubject() {
        return new ProjectedJsonObject(original,
                new HashSet<>(asList("minimum", "maximum")));
    }

    @Test
    public void childForReturnsJsonObject_withoutProjection() {
        JsonValue actual = createSubject().childFor("not");
        assertEquals(original.childFor("not"), actual);
    }

    @Test
    public void containsKeySuccess() {
        assertTrue(createSubject().containsKey("not"));
    }

    @Test
    public void containsKeyIsFalseForHiddenKeys() {
        assertFalse(createSubject().containsKey("minimum"));
    }

    @Test
    public void requireWithConsumerSucceeds() {
        Consumer<JsonValue> consumer = mock(Consumer.class);
        createSubject().require("not", consumer);
        verify(consumer).accept(JsonValue.of(original.get("not")));
    }

    @Test(expected = SchemaException.class)
    public void requireWithConsumerHidesKeys() {
        Consumer<JsonValue> consumer = mock(Consumer.class);
        createSubject().require("minimum", consumer);
    }

    @Test
    public void requireSucceeds() {
        JsonValue actual = createSubject().require("not");
        assertEquals(JsonValue.of(original.get("not")), actual);
    }

    @Test(expected = SchemaException.class)
    public void requireHidesKeys() {
        createSubject().require("minimum");
    }

    @Test
    public void requireMappingSucceeds() {
        Boolean actual = createSubject().requireMapping("not", val -> true);
        assertTrue(actual);
    }

    @Test(expected = SchemaException.class)
    public void requireMappingFailsForHiddenKey() {
        createSubject().requireMapping("minimum", val -> true);
    }

    @Test
    public void requireObjectSuccess() {
        Function<JsonObject, Object> mapper = mock(Function.class);
        ProjectedJsonObject subject = createSubject();
        subject.requireObject(mapper);
        verify(mapper).apply(subject);
    }

    @Test
    public void maybeSucceeds() {
        assertTrue(createSubject().maybe("not").isPresent());
    }

    @Test
    public void maybeHidesHiddenKeys() {
        assertFalse(createSubject().maybe("minimum").isPresent());
    }

    @Test
    public void maybeWithConsumerSucceeds() {
        Consumer<JsonValue> consumer = mock(Consumer.class);
        createSubject().maybe("not", consumer);
        verify(consumer).accept(JsonValue.of(original.get("not")));
    }

    @Test
    public void maybeWithConsumerHidesHiddenKeys() {
        Consumer<JsonValue> consumer = mock(Consumer.class);
        createSubject().maybe("minimum", consumer);
        verify(consumer, never()).accept(any());
    }

    @Test
    public void maybeMappingSucceeds() {
        assertTrue(createSubject().maybeMapping("not", val -> true).isPresent());
    }

    @Test
    public void maybeMappingHidesKey() {
        assertFalse(createSubject().maybeMapping("minimum", val -> true).isPresent());
    }

    @Test
    public void forEachOmitsHiddenKeys() {
        JsonObjectIterator iterator = mock(JsonObjectIterator.class);
        createSubject().forEach(iterator);
        verify(iterator).apply("not", JsonValue.of(original.get("not")));
        verifyNoMoreInteractions(iterator);
    }

    @Test
    public void keySetExcludesHiddenKeys() {
        assertEquals(singleton("not"), createSubject().keySet());
    }

    @Test
    public void isEmptyTakesHiddenKeysIntoAccount() {
        assertFalse(createSubject().isEmpty());

        ProjectedJsonObject subject = new ProjectedJsonObject(new JsonObject(ImmutableMap.<String, Object>builder()
                .put("minimum", 2)
                .put("maximum", 20).build()), new HashSet<>(asList("minimum", "maximum")));
        assertTrue(subject.isEmpty());
    }

    @Test
    public void unwrapExcludesHiddenKeys() {
        Map<String, Object> unwrapped = (Map<String, Object>) createSubject().unwrap();
        assertEquals(singleton("not"), unwrapped.keySet());
    }

    @Test
    public void toMapExcludesHiddenKeys() {
        Map<String, Object> map = createSubject().toMap();
        assertEquals(singleton("not"), map.keySet());
    }

}
