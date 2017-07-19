package org.everit.json.schema.internal;

import static org.everit.json.schema.internal.ValidatorTestSupport.assertFailure;
import static org.everit.json.schema.internal.ValidatorTestSupport.assertSuccess;

import org.junit.Test;

public class URITemplateFormatTest {

    private final URITemplateFormatValidator subject = new URITemplateFormatValidator();

    @Test
    public void success() {
        assertSuccess("http://example.com/dictionary/{term:1}/{term}", subject);
    }

    @Test
    public void unclosedBracket() {
        assertFailure("http://example.com/dictionary/{term:1}/{term", subject,
                "[http://example.com/dictionary/{term:1}/{term] is not a valid URI template");
    }
}
