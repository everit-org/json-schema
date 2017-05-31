package org.everit.json.schema;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class FalseSchemaTest {

    @Test public void alwaysFails() {
        TestSupport.failureOf(FalseSchema.builder())
                .input("whatever")
                .expectedKeyword("false")
                .expect();
    }

    @Test
    public void toStringTest() {
        assertEquals("false", FalseSchema.builder().build().toString());
    }
}
