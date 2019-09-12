package org.everit.json.schema;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InternalValidationExceptionTest {

    @Test
    public void stackTraceShouldBeEmpty() {
        try {
            throw new InternalValidationException(BooleanSchema.INSTANCE, "message", "keyword", "#");
        } catch (ValidationException e) {
            assertEquals(0, e.getStackTrace().length);
        }
    }
}
