package org.everit.json.schema.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Common superclass for {@link IPV4Validator} and {@link IPV6Validator}.
 */
@Deprecated
public class IPAddressValidator {

    /**
     * Creates an {@link InetAddress} instance if possible and returns it, or on failure it returns
     * {@code Optional.empty()}.
     *
     * @param subject
     *         the string to be validated.
     * @return the optional validation failure message
     */
    protected Optional<InetAddress> asInetAddress(final String subject) {
        if (subject == null)
            return Optional.empty();

        try {
            return Optional.of(InetAddress.getByName(subject));
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    /**
     * Checks an IP address.
     *
     * @param subject
     *         the string to be validated.
     * @param expectedLength
     *         the expected length of {@code subject} - it is validated if
     *         {@link #asInetAddress(String)} validation succeeds.
     * @param failureFormat
     *         the {@link String#format(String, Object...) string format} of the validation failue
     *         message. The format string will receive only the {@code subject} parameter (so it has
     *         to be referred as {@code %s} in the format string
     * @return the optional validation failure message
     */
    protected Optional<String> checkIpAddress(final String subject, final int expectedLength,
                                              final String failureFormat) {
        return asInetAddress(subject)
                .filter(addr -> addr.getAddress().length == expectedLength)
                .map(addr -> emptyString())
                .orElse(Optional.of(String.format(failureFormat, subject)));
    }

    private Optional<String> emptyString() {
        return Optional.empty();
    }

}
