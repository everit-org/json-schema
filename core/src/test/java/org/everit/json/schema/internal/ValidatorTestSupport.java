package org.everit.json.schema.internal;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.everit.json.schema.FormatValidator;
import org.junit.Assert;

public final class ValidatorTestSupport {

    static void assertSuccess(String subject, FormatValidator format) {
        assertSuccess(subject, format, null);
    }

    static void assertSuccess(String subject, FormatValidator format, Map<String, Object> unprocessedProperties) {
        Optional<String> opt = format.validate(subject, unprocessedProperties);
        Assert.assertNotNull("the optional is not null", opt);
        Assert.assertFalse("failure not exist", opt.isPresent());
    }

    static void assertFailure(String subject, FormatValidator format, String expectedFailure) {
        assertFailure(subject, format, null, expectedFailure);
    }

    static void assertFailure(String subject, FormatValidator format, Map<String, Object> unprocessedProperties, String expectedFailure) {
        Optional<String> opt = format.validate(subject, unprocessedProperties);
        Assert.assertNotNull("the optional is not null", opt);
        assertTrue("failure exists", opt.isPresent());
        Assert.assertEquals(expectedFailure, opt.get());
    }

    private ValidatorTestSupport() {
    }
}
