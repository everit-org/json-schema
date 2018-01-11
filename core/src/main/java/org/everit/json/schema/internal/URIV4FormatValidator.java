package org.everit.json.schema.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.everit.json.schema.FormatValidator;

public class URIV4FormatValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        try {
            URI uri = new URI(subject);
            return Optional.empty();
        } catch (URISyntaxException | NullPointerException e) {
            return failure(subject);
        }
    }

    protected Optional<String> failure(String subject) {
        return Optional.of(String.format("[%s] is not a valid URI", subject));
    }

    @Override public String formatName() {
        return "uri";
    }
}
