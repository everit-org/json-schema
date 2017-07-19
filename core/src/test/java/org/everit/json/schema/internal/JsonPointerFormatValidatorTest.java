package org.everit.json.schema.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Optional;

import org.junit.Test;

public class JsonPointerFormatValidatorTest {

    private final JsonPointerFormatValidator subject = new JsonPointerFormatValidator();

    @Test
    public void uriSuccess() {
        assertEmpty(subject.validate("#/hello"));
    }

    private void assertEmpty(Optional<String> actual) {
        assertSame(Optional.empty(), actual);
    }

    @Test
    public void stringSuccess() {
        assertEmpty(subject.validate("/hello"));
    }

    @Test
    public void illegalLeadingCharFailure() {
        assertEquals(Optional.of("[aaa] is not a valid JSON pointer"), subject.validate("aaa"));
    }

}
