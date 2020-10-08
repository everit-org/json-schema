package org.everit.json.schema.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
