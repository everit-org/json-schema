package org.everit.json.schema.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONString;
import org.junit.Test;

public class JSONWriterTest {

    public static class Ancestor implements JSONString {

        @Override public String toJSONString() {
            return null;
        }

    }

    public static class Descendant extends Ancestor {

    }

    @Test
    public void classImplementsJSONString() {
        assertTrue(JSONWriter.implementsJSONString(new Ancestor()));
    }

    @Test
    public void subclassImplementsJSONString() {
        assertTrue(JSONWriter.implementsJSONString(new Descendant()));
    }

    @Test
    public void nullDoesNotImplementJSONString() {
        assertFalse(JSONWriter.implementsJSONString(null));
    }

    @Test
    public void objectDoesNotImplementJSONString() {
        assertFalse(JSONWriter.implementsJSONString(new Object()));
    }

}
