package org.everit.json.schema.regexp;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class AbstractRegexp implements Regexp {

    private final String asString;

    public AbstractRegexp(String asString) {
        this.asString = requireNonNull(asString, "asString cannot be null");
    }

    @Override
    public Optional<RegexpMatchingFailure> patternMatchingFailure(String input) {
        // Your implementation here
        return Optional.empty();
    }

    @Override
    public String toString() {
        return asString;
    }
}
