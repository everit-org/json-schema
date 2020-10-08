package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;

import static org.junit.jupiter.api.Assertions.*;

public final class ValidatorTestSupport {

    static void assertSuccess(String subject, FormatValidator format) {
        Optional<String> opt = format.validate(subject);
        assertNotNull(opt, "the optional is not null");
        assertFalse(opt.isPresent(), "failure not exist");
    }

    static void assertFailure(String subject, FormatValidator format, String expectedFailure) {
        Optional<String> opt = format.validate(subject);
        assertNotNull(opt, "the optional is not null");
        assertTrue(opt.isPresent(), "failure exists");
        assertEquals(expectedFailure, opt.get());
    }

    private ValidatorTestSupport() {
    }
}
