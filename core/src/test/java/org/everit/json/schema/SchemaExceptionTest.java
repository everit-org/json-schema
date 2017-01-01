package org.everit.json.schema;

import org.json.JSONPointer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.everit.json.schema.SchemaException.buildMessage;
import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class SchemaExceptionTest {

    @Rule
    public final ExpectedException expExc = ExpectedException.none();

    @Test
    public void testBuildMessageSingleExcType() {
        String actual = buildMessage(JSONPointer.builder().build(), 42, String.class);
        assertEquals("#: expected type: String, found: Integer", actual);
    }

    @Test
    public void nullJSONPointer() {
        expExc.expect(NullPointerException.class);
        expExc.expectMessage("pointer cannot be null");
        buildMessage(null, 42, String.class);
    }

    @Test
    public void nullActual() {
        JSONPointer ptr = JSONPointer.builder().append("required").append("2").build();
        String actual = buildMessage(ptr, null, String.class);
        assertEquals("#/required/2: expected type: String, found: null", actual);
    }

    @Test
    public void twoExpected() {
        String actual = buildMessage(JSONPointer.builder().build(), 42, String.class, Map.class);
        assertEquals("#: expected type is one of String or Map, found: Integer", actual);
    }

}
