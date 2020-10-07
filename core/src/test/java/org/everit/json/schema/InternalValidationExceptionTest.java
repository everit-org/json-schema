package org.everit.json.schema;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
