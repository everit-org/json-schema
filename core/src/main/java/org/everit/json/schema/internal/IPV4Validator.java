package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;

/**
 * Implementation of the "ipv4" format value.
 */
public class IPV4Validator extends IPAddressValidator implements FormatValidator {

    private static final int IPV4_LENGTH = 4;

    @Override
    public Optional<String> validate(final String subject) {
        return checkIpAddress(subject, IPV4_LENGTH, "[%s] is not a valid ipv4 address");
    }

    @Override
    public String formatName() {
        return "ipv4";
    }
}
