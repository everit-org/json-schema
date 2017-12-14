package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.everit.json.schema.loader.JsonObjectTest.mockConsumer;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.function.Consumer;

import org.everit.json.schema.SchemaException;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * @author erosb
 */
@RunWith(JUnitParamsRunner.class)
public class JsonValueTest {

    static final JsonValue asV6Value(Object o) {
        LoaderConfig v6Config = new LoaderConfig(new DefaultSchemaClient(), emptyMap(), SpecificationVersion.DRAFT_6, false);
        LoadingState ls = new LoadingState(v6Config, emptyMap(), JsonValue.of(o), JsonValue.of(o), null, emptyList());
        return ls.schemaJson;
    }

    static final JsonValue withLs(Object o) {
        LoaderConfig v4Config = new LoaderConfig(new DefaultSchemaClient(), emptyMap(), SpecificationVersion.DRAFT_4, false);
        LoadingState ls = new LoadingState(v4Config, new HashMap<>(), o, o, null, emptyList());
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

    @Rule
    public ExpectedException exc = ExpectedException.none();

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
        Integer actual = JsonValue.of("42").requireString(e -> Integer.valueOf(e));
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
        assertTrue(FLS.requireBoolean(bool -> !bool));
    }

    @Test
    public void requireNumberFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: Number, found: JsonObject");
        OBJ.requireNumber();
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
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: Integer, found: JsonArray");
        ARR.requireInteger();
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
        String actual = OBJ.requireObject(obj -> "hello");
        assertEquals("hello", actual);
    }

    @Test
    public void requireArrayFailure() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type: JsonArray, found: JsonObject");
        OBJ.requireArray();
    }

    @Test
    public void requireArrayWithMapping() {
        Integer actual = ARR.requireArray(arr -> arr.length());
        assertEquals(2, actual.intValue());
    }

    private Object[] par(Object raw, Class<?> expectedRetType) {
        return new Object[] { raw, expectedRetType };
    }

    private Object[] providerTestFactory() {
        return new Object[] {
                par(null, JsonValue.class),
                par(emptyMap(), JsonObject.class),
                par(emptyList(), JsonArray.class),
                par(new JSONObject(), JsonObject.class),
                par(new JSONArray(), JsonArray.class)
        };
    }

    @Test
    @Parameters(method = "providerTestFactory")
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
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type is one of Boolean or String, found: Integer");
        INT.canBe(String.class, str -> {
        })
                .or(Boolean.class, bool -> {
                })
                .requireAny();
    }

    @Test
    public void multiplexFailureForNullValue() {
        exc.expect(SchemaException.class);
        exc.expectMessage("#: expected type is one of Boolean or String, found: null");
        withLs(JsonValue.of(null)).canBe(String.class, s -> {
        })
                .or(Boolean.class, b -> {
                })
                .requireAny();
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
