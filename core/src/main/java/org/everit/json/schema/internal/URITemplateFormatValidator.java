package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;

public class URITemplateFormatValidator implements FormatValidator {

    @Override public Optional<String> validate(String subject) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override public String formatName() {
        return "uri-template";
    }
}
