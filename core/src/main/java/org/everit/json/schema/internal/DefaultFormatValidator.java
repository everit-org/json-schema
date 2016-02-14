package org.everit.json.schema.internal;

import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;

import org.everit.json.schema.Format;
import org.everit.json.schema.FormatValidator;

import com.google.common.net.InetAddresses;

public class DefaultFormatValidator implements FormatValidator {

  private static final int IPV4_LENGTH = 4;

  private static final int IPV6_LENGTH = 16;

  private Optional<InetAddress> asInetAddress(final String subject) {
    if (InetAddresses.isInetAddress(subject)) {
      return Optional.of(InetAddresses.forString(subject));
    } else {
      return Optional.empty();
    }
  }

  private Optional<String> checkIpAddress(final String subject, final int expectedLength,
      final String failureFormat) {
    return asInetAddress(subject)
        .filter(addr -> addr.getAddress().length == expectedLength)
        .map(addr -> Optional.<String> empty())
        .orElse(Optional.of(String.format(failureFormat, subject)));
  }

  @Override
  public Optional<String> validate(final String subject, final Format format) {
    Objects.requireNonNull(format, "format cannot be null");
    switch (format) {
      case IPV4:
        return checkIpAddress(subject, IPV4_LENGTH, "[%s] is not a valid ipv4 address");
      case IPV6:
        return checkIpAddress(subject, IPV6_LENGTH, "[%s] is not a valid ipv6 address");
      default:
        throw new IllegalArgumentException("unsupported format: " + format);
    }
  }
}
