package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;

/**
 * Implementation of the "ipv6" format value.
 */
public class IPV6Validator extends IPAddressValidator implements FormatValidator {

    private static final int IPV6_LENGTH = 16;

    @Override
    public Optional<String> validate(final String subject) {
        return checkIpAddress(subject, IPV6_LENGTH, "[%s] is not a valid ipv6 address");
    }

    @Override
    public String formatName() {
        return "ipv6";
    }
}
