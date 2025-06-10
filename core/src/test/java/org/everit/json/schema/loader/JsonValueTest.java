package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.everit.json.schema.loader.JsonObjectTest.mockConsumer;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.everit.json.schema.SchemaException;
import org.everit.json.schema.SchemaLocation;
import org.everit.json.schema.loader.internal.DefaultProviderValidators;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author erosb
 */
public class JsonValueTest {

    static final JsonValue asV6Value(Object o) {
        LoaderConfig v6Config = new LoaderConfig(new DefaultSchemaClient(), new DefaultProviderValidators(), SpecificationVersion.DRAFT_6, false);
        LoadingState ls = new LoadingState(v6Config, emptyMap(), JsonValue.of(o), JsonValue.of(o), null, SchemaLocation.empty());
        return ls.schemaJson;
    }

    static final JsonValue withLs(Object o) {
        LoaderConfig v4Config = new LoaderConfig(new DefaultSchemaClient(), new DefaultProviderValidators(), SpecificationVersion.DRAFT_4, false);
        LoadingState ls = new LoadingState(v4Config, new HashMap<>(), o, o, null, SchemaLocation.empty());
        return ls.schemaJson;
    }

    public static final JsonValue INT = JsonValue.of(3);

    public static final JsonValue OBJ = new JsonObject(emptyMap());

    public static final JsonValue FLS = JsonValue.of(false);

    public static final JsonValue TRU = JsonValue.of(true);

    public static final JsonValue STR = JsonValue.of("string");

    public static final JsonValue ARR = JsonValue.of(asList(true, 42));

    static {
        asList(INT, OBJ, FLS, TRU, STR, ARR).forEach(schemaJson -> {
            withLs(schemaJson);
        });
    }

    @Test
    public void requireStringFailure() {
        SchemaException thrown = assertThrows(SchemaException.class, () -> {
            TRU.requireString();
        });
        assertEquals("#: expected type: String, found: Boolean", thrown.getMessage());
    }

    @Test
    public void requireStringSuccess() {
        assertEquals("string", STR.requireString());
    }

    @Test
    public void requireStringWithMapper() {
        Integer actual = JsonValue.of("42").requireString(e -> Integer.valueOf(e));
        assertEquals(Integer.valueOf(42), actual);
    }

    @Test
    public void requireBooleanFailure() {
        SchemaException thrown = assertThrows(SchemaException.class, () -> {
            STR.requireBoolean();
        });
        assertEquals("#: expected type: Boolean, found: String", thrown.getMessage());
    }

    @Test
    public void requireBooleanSuccess() {
        assertTrue(TRU.requireBoolean());
    }

    @Test
    public void requireBooleanWithMapper() {
        assertTrue(FLS.requireBoolean((Function<Boolean, Boolean>) bool -> !bool));
    }

    @Test
    public void requireNumberFailure() {
        SchemaException thrown = assertThrows(SchemaException.class, () -> {
            OBJ.requireNumber();
        });
        assertEquals("#: expected type: Number, found: JsonObject", thrown.getMessage());
    }

    @Test
    public void requireNumberSuccess() {
        assertEquals(3.14, JsonValue.of(3.14).requireNumber());
    }

    @Test
    public void requireNumberWithMapping() {
        assertEquals(Integer.valueOf(3), JsonValue.of(3.14).requireNumber(d -> Integer.valueOf(d.intValue())));
    }

    @Test
    public void requireIntegerFailure() {
        SchemaException thrown = assertThrows(SchemaException.class, () -> {
            ARR.requireInteger();
        });
        assertEquals("#: expected type: Integer, found: JsonArray", thrown.getMessage());
    }

    @Test
    public void requireIntegerSuccess() {
        assertEquals(3, INT.requireInteger().intValue());
    }

    @Test
    public void requireIntegerWithMapper() {
        assertEquals(4, INT.requireInteger(i -> i.intValue() + 1).intValue());
    }

    @Test
    public void requireObjectFailure() {
        SchemaException thrown = assertThrows(SchemaException.class, () -> {
            STR.requireObject();
        });
        assertEquals("#: expected type: JsonObject, found: String", thrown.getMessage());
    }

    @Test
    public void requireObjectSuccess() {
        JsonObject actual = OBJ.requireObject();
        assertSame(OBJ, actual);
    }

    @Test
    public void requireObjectWithMapping() {
        String actual = OBJ.requireObject(obj -> "hello");
        assertEquals("hello", actual);
    }

    @Test
    public void requireArrayFailure() {
        SchemaException thrown = assertThrows(SchemaException.class, () -> {
            OBJ.requireArray();
        });
        assertEquals("#: expected type: JsonArray, found: JsonObject", thrown.getMessage());
    }

    @Test
    public void requireArrayWithMapping() {
        Integer actual = ARR.requireArray(arr -> arr.length());
        assertEquals(2, actual.intValue());
    }

    private static List<Arguments> providerTestFactory() {
        return Stream.of(
                Arguments.of(null, JsonValue.class),
                Arguments.of(emptyMap(), JsonObject.class),
                Arguments.of(emptyList(), JsonArray.class),
                Arguments.of(new JSONObject(), JsonObject.class),
                Arguments.of(new JSONArray(), JsonArray.class)
        ).collect(Collectors.toList());
    }

    @ParameterizedTest
    @MethodSource("providerTestFactory")
    public void testFactory(Object raw, Class<?> expectedRetType) {
        assertThat(JsonValue.of(raw), is(instanceOf(expectedRetType)));
    }

    @Test
    public void testMultiplexer() {
        Consumer<JsonArray> arrConsumer = mockConsumer();
        Consumer<JsonObject> objConsumer = mockConsumer();
        ARR.canBe(JsonArray.class, arrConsumer)
                .or(JsonObject.class, objConsumer)
                .requireAny();
        verify(arrConsumer).accept(ARR.requireArray());
        verify(objConsumer, never()).accept(any());
    }

    @Test
    public void multiplexerWithObject() {
        Consumer<JsonObject> objConsumer = mockConsumer();
        OBJ.canBe(JsonObject.class, objConsumer).requireAny();
        verify(objConsumer).accept(OBJ.requireObject());
    }

    @Test
    public void multiplexerWithPrimitives() {
        Consumer<String> consumer = mockConsumer();
        STR.canBe(String.class, consumer)
                .or(Boolean.class, bool -> {
                })
                .requireAny();
        verify(consumer).accept(STR.requireString());
    }

    @Test
    public void multiplexerFailure() {
        SchemaException thrown = assertThrows(SchemaException.class, () -> {
            INT.canBe(String.class, str -> {
            })
                    .or(Boolean.class, bool -> {
                    })
                    .requireAny();
        });
        assertEquals("#: expected type is one of Boolean or String, found: Integer", thrown.getMessage());
    }

    @Test
    public void multiplexFailureForNullValue() {
        SchemaException thrown = assertThrows(SchemaException.class, () -> {
            withLs(JsonValue.of(null)).canBe(String.class, s -> {
            })
                    .or(Boolean.class, b -> {
                    })
                    .requireAny();
        });
        assertEquals("#: expected type is one of Boolean or String, found: null", thrown.getMessage());
    }

    @Test
    public void canBeSchemaMatchesObject() {
        Consumer<JsonValue> ifSchema = spy(schemaConsumer());
        JsonValue subject = withLs(JsonValue.of(emptyMap()));
        subject.canBeSchema(ifSchema).requireAny();
        verify(ifSchema).accept(subject);
    }

    protected Consumer<JsonValue> schemaConsumer() {
        return new Consumer<JsonValue>() {

            @Override public void accept(JsonValue jsonValue) {

            }
        };
    }

    @Test
    public void booleanCannotBeSchemaIfV4() {
        Consumer<JsonValue> ifSchema = spy(schemaConsumer());
        JsonValue subject = withLs(JsonValue.of(true));
        try {
            subject.canBeSchema(ifSchema).requireAny();
            fail("did not throw exception");
        } catch (SchemaException e) {
            assertEquals("#: expected type is one of JsonObject, found: Boolean", e.getMessage());
            verify(ifSchema, never()).accept(any());
        }
    }

    @Test
    public void booleanCanBeSchemaIfV6() {
        Consumer<JsonValue> ifSchema = spy(schemaConsumer());
        JsonValue subject = asV6Value(true);
        subject.canBeSchema(ifSchema).requireAny();
        verify(ifSchema).accept(subject);
    }

    @Test
    public void canBeSchemaWithV6hasProperExceptionMessage() {
        Consumer<JsonValue> ifSchema = spy(schemaConsumer());
        JsonValue subject = asV6Value(42);
        try {
            subject.canBeSchema(ifSchema).requireAny();
        } catch (SchemaException e) {
            assertEquals("#: expected type is one of Boolean or JsonObject, found: Integer", e.getMessage());
        }
        verify(ifSchema, never()).accept(subject);
    }

}
