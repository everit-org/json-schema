package org.everit.json.schema.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.everit.json.schema.FormatValidator;

/**
 * Implementation of the "uri" format value.
 */
public class URIFormatValidator implements FormatValidator {

    private final boolean protocolRelativeURIPermitted;

    public URIFormatValidator() {
        this(true);
    }

    public URIFormatValidator(boolean protocolRelativeURIPermitted) {
        this.protocolRelativeURIPermitted = protocolRelativeURIPermitted;
    }

    @Override
    public Optional<String> validate(final String subject) {
        try {
            URI uri = new URI(subject);
            if (hasProtocol(uri) || (protocolRelativeURIPermitted && isProtocolRelativeURI(subject))) {
                return Optional.empty();
            } else {
                throw new URISyntaxException(subject, "no protocol and not protocol-relative");
            }
        } catch (URISyntaxException | NullPointerException e) {
            return failure(subject);
        }
    }

    protected Optional<String> failure(String subject) {
        return Optional.of(String.format("[%s] is not a valid URI", subject));
    }

    private boolean isProtocolRelativeURI(String subject) {
        return subject.startsWith("//");
    }

    private boolean hasProtocol(URI uri) {
        return uri.getScheme() != null;
    }

    @Override public String formatName() {
        return "uri";
    }
}
