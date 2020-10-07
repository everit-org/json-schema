package org.everit.json.schema.regexp;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JavaUtilRegexpTest {

    static final String PATTERN = "^aa.*b$";

    private Regexp createHandler() {
        return new JavaUtilRegexpFactory().createHandler(PATTERN);
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
        EqualsVerifier.forClass(JavaUtilRegexp.class)
            .withPrefabValues(Pattern.class, Pattern.compile("red"), Pattern.compile("black"))
            .withIgnoredFields("asString").withNonnullFields("pattern")
            .suppress(Warning.STRICT_INHERITANCE)
            .verify();
    }

}
