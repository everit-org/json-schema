package org.everit.json.schema;

import org.json.JSONPointer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.everit.json.schema.SchemaException.buildMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author erosb
 */
public class SchemaExceptionTest {

    private static final Class<?> INTEGER_CLASS = Integer.class;

    @Test
    public void testBuildMessageSingleExcType() {
        String actual = buildMessage(JSONPointer.builder().build().toURIFragment(), INTEGER_CLASS, String.class);
        assertEquals("#: expected type: String, found: Integer", actual);
    }

    @Test
    public void nullJSONPointer() {
        NullPointerException thrown = assertThrows(NullPointerException.class, () -> {
            buildMessage(null, INTEGER_CLASS, String.class);
        });
        assertEquals("pointer cannot be null", thrown.getMessage());
    }

    @Test
    public void nullActual() {
        JSONPointer ptr = JSONPointer.builder().append("required").append("2").build();
        String actual = buildMessage(ptr.toURIFragment(), null, String.class);
        assertEquals("#/required/2: expected type: String, found: null", actual);
    }

    @Test
    public void twoExpected() {
        String actual = buildMessage(JSONPointer.builder().build().toURIFragment(), INTEGER_CLASS, String.class, Map.class);
        assertEquals("#: expected type is one of String or Map, found: Integer", actual);
    }

}
