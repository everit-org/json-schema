package org.everit.json.schema.internal;

import static org.everit.json.schema.internal.ValidatorTestSupport.assertFailure;
import static org.everit.json.schema.internal.ValidatorTestSupport.assertSuccess;

import org.junit.Test;

public class JsonPointerFormatValidatorTest {

    private final JsonPointerFormatValidator subject = new JsonPointerFormatValidator();

    @Test
    public void uriSuccess() {
        assertSuccess("#/hello", subject);
    }

    @Test
    public void stringSuccess() {
        assertSuccess("/hello", subject);
    }

    @Test
    public void illegalLeadingCharFailure() {
        assertFailure("aaa", subject, "[aaa] is not a valid JSON pointer");
    }

}
