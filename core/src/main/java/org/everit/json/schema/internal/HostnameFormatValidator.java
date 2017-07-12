package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;

import com.google.common.net.InternetDomainName;

/**
 * Implementation of the "hostname" format value.
 */
public class HostnameFormatValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        try {
            InternetDomainName.from(subject);
            return Optional.empty();
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.of(String.format("[%s] is not a valid hostname", subject));
        }
    }

    @Override
    public String formatName() {
        return "hostname";
    }
}
