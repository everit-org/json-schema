package org.everit.json.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author erosb
 */
public class TrueSchemaTest {

    @Test
    public void testToString() {
        assertEquals("true", TrueSchema.builder().build().toString());
    }

}
