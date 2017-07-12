package org.everit.json.schema.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.everit.json.schema.FormatValidator;

/**
 * Implementation of the "uri" format value.
 */
public class URIFormatValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        try {
            new URI(subject);
            return Optional.empty();
        } catch (URISyntaxException | NullPointerException e) {
            return Optional.of(String.format("[%s] is not a valid URI", subject));
        }
    }

    @Override public String formatName() {
        return "uri";
    }
}
