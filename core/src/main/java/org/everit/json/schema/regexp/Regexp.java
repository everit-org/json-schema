package org.everit.json.schema.regexp;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

public interface Regexp {

    Optional<RegexpMatchingFailure> patternMatchingFailure(String input);

    String toString();
}

