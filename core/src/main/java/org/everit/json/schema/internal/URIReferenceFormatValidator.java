package org.everit.json.schema.internal;

import static java.lang.String.format;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.everit.json.schema.FormatValidator;

public class URIReferenceFormatValidator implements FormatValidator {

    @Override public Optional<String> validate(String subject) {
        try {
            new URI(subject);
            return Optional.empty();
        } catch (URISyntaxException e) {
            return failure(subject);
        }
    }

    protected Optional<String> failure(String subject) {
        return Optional.of(format("[%s] is not a valid URI reference", subject));
    }

    @Override public String formatName() {
        return "uri-reference";
    }
}
