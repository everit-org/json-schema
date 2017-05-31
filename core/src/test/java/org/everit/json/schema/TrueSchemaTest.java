package org.everit.json.schema;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class TrueSchemaTest {

    @Test
    public void testToString() {
        assertEquals("true", TrueSchema.builder().build().toString());
    }

}
