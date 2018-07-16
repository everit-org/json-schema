package org.everit.json.schema.internal;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.everit.json.schema.FormatValidator;

import java.util.Optional;

/**
 * Implementation of the "ipv6" format value.
 */
public class IPV6Validator extends IPAddressValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        return (subject != null) && InetAddressValidator.getInstance().isValidInet6Address(subject) ?
                Optional.empty() :
                Optional.of(String.format("[%s] is not a valid ipv6 address", subject));
    }

    @Override
    public String formatName() {
        return "ipv6";
    }
}
