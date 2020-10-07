package org.everit.json.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
