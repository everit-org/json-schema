package org.everit.json.schema.loader;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.everit.json.schema.SchemaException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;

/**
 * @author erosb
 */
@RunWith(JUnitParamsRunner.class)
public class JsonValueTest {

    static final LoadingState emptyLs = new LoadingState(SchemaLoader.builder()
            .rootSchemaJson(new JSONObject())
            .schemaJson(new JSONObject()));
    
    public static final JsonValue OBJ = new JsonObject(emptyMap(), emptyLs);

    public static final JsonValue FLS = JsonValue.of(false, emptyLs);


    public static final JsonValue TRU = JsonValue.of(true, emptyLs);

    public static final JsonValue STR = JsonValue.of("string", emptyLs);

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
        Integer actual = JsonValue.of("42", emptyLs).requireString((e, ls) -> Integer.valueOf(e));
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
        assertTrue(FLS.requireBoolean((bool, ls) -> !bool));
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
        String actual = OBJ.requireObject((obj, ls) -> "hello");
        assertEquals("hello", actual);
    }

    private Object[] par(Object raw, Class<?> expectedRetType) {
        return new Object[]{raw, expectedRetType};
    }

    private Object[] providerTestFactory() {
        return new Object[]{
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
        assertThat(JsonValue.of(raw, emptyLs), is(instanceOf(expectedRetType)));
    }


}
