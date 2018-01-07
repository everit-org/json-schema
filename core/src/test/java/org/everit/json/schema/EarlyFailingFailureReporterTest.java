package org.everit.json.schema;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

public class EarlyFailingFailureReporterTest {

    private EarlyFailingFailureReporter createSubject() {
        return new EarlyFailingFailureReporter(NullSchema.INSTANCE);
    }

    @Test
    public void testFailure() {
        EarlyFailingFailureReporter subject = createSubject();
        ValidationException input = new ValidationException(BooleanSchema.INSTANCE, Boolean.class, "string");
        try {
            subject.failure(input);
            fail();
        } catch (ValidationException e) {
            assertSame(input, e);
        }
    }

    @Test
    public void testValidationFinished() {
        // should be no-op
        createSubject().validationFinished();
    }

}
