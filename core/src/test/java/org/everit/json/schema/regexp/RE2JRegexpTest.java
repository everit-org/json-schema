package org.everit.json.schema.regexp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Optional;

import org.junit.Test;

public class RE2JRegexpTest {

    static final String PATTERN = "^aa.*b$";

    private Regexp createHandler() {
        return new RE2JRegexpFactory().createHandler(PATTERN);
    }

    @Test
    public void success() {
        assertSame(Optional.empty(), createHandler().patternMatchingFailure("aaaaaaaaab"));
    }

    @Test
    public void failure() {
        assertEquals(Optional.of(new RegexpMatchingFailure()), createHandler().patternMatchingFailure("xxx"));
    }

    @Test
    public void asString() {
        assertEquals(PATTERN, createHandler().toString());
    }

}
