package org.everit.json.schema.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class URIV4FormatValidatorTest {

    @Test
    public void relativeURI() {
        assertFalse(new URIV4FormatValidator().validate("abc").isPresent());
    }

    @Test
    public void absoluteURI() {
        assertFalse(new URIV4FormatValidator().validate("http://a.b.c").isPresent());
    }

    @Test
    public void notURI() {
        assertTrue(new URIV4FormatValidator().validate("\\\\\\\\WINDOWS\\\\fileshare").isPresent());
    }
}
