package org.everit.json.schema.internal;

import static java.lang.String.format;
import static org.everit.json.schema.internal.ValidatorTestSupport.assertSuccess;

import org.junit.Test;

public class JsonPointerFormatValidatorTest {

    private final JsonPointerFormatValidator subject = new JsonPointerFormatValidator();

    @Test
    public void stringSuccess() {
        assertSuccess("/hello", subject);
    }

    @Test
    public void root() {
        assertSuccess("/", subject);
    }

    @Test
    public void emptyStringIsValid() {
        assertSuccess("", subject);
    }

    @Test
    public void illegalLeadingCharFailure() {
        assertFailure("aaa");
    }

    @Test
    public void invalidTildeEscape() {
        assertFailure("/~asd");
    }

    @Test
    public void invalidEscapeNum() {
        assertFailure("/~2");
    }

    @Test
    public void trailingTilde() {
        assertFailure("/foo/bar~");
    }

    @Test
    public void uriFragment() {
        assertFailure("#/");
    }

    private void assertFailure(String input) {
        ValidatorTestSupport.assertFailure(input, subject, format("[%s] is not a valid JSON pointer", input));
    }

}
