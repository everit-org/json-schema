package org.everit.json.schema.internal;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;
import org.junit.Assert;

public final class ValidatorTestSupport {

    static void assertSuccess(final String subject, final FormatValidator format) {
        Optional<String> opt = format.validate(subject);
        Assert.assertNotNull("the optional is not null", opt);
        Assert.assertFalse("failure not exist", opt.isPresent());
    }

    static void assertFailure(final String subject, final FormatValidator format,
            final String expectedFailure) {
        Optional<String> opt = format.validate(subject);
        Assert.assertNotNull("the optional is not null", opt);
        assertTrue("failure exists", opt.isPresent());
        Assert.assertEquals(expectedFailure, opt.get());
    }

    private ValidatorTestSupport() {
    }
}
