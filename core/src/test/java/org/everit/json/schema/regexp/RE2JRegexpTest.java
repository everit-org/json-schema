package org.everit.json.schema.regexp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Optional;

import com.google.re2j.Pattern;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

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

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(RE2JRegexp.class)
            .withPrefabValues(Pattern.class, Pattern.compile("red"), Pattern.compile("black"))
            .withIgnoredFields("asString")
            .suppress(Warning.STRICT_INHERITANCE)
            .verify();
    }

}
