package org.everit.json.schema;

import java.util.Optional;

public class EvenCharNumValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        if (subject.length() % 2 == 0) {
            return Optional.empty();
        } else {
            return Optional.of(String.format("the length of srtring [%s] is odd", subject));
        }
    }

    @Override
    public String formatName() {
        return "evenlength";
    }
}
