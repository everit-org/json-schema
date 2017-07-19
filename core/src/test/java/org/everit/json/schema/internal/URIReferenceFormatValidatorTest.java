package org.everit.json.schema.internal;

import static org.everit.json.schema.internal.ValidatorTestSupport.assertFailure;
import static org.everit.json.schema.internal.ValidatorTestSupport.assertSuccess;

import org.junit.Test;

public class URIReferenceFormatValidatorTest {

    private final URIReferenceFormatValidator subject = new URIReferenceFormatValidator();

    @Test
    public void success() {
        assertSuccess("http://foo.bar/?baz=qux#quux", subject);
    }

    @Test
    public void protocolRelativeRef() {
        assertSuccess("//foo.bar/?baz=qux#quux", subject);
    }

    @Test
    public void pathSuccess() {
        assertSuccess("/abc", subject);
    }

    @Test
    public void illegalCharFailure() {
        assertFailure("\\\\WINDOWS\\fileshare", subject, "[\\\\WINDOWS\\fileshare] is not a valid URI reference");
    }

}
