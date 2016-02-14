/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema.internal;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.validator.routines.EmailValidator;
import org.everit.json.schema.Format;
import org.everit.json.schema.FormatValidator;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

public class DefaultFormatValidator implements FormatValidator {

  private static final int IPV4_LENGTH = 4;

  private static final int IPV6_LENGTH = 16;

  private static final String DATETIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssXXX";

  private static final String DATETIME_FORMAT_STRING_SECFRAC = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  private Optional<InetAddress> asInetAddress(final String subject) {
    try {
      if (InetAddresses.isInetAddress(subject)) {
        return Optional.of(InetAddresses.forString(subject));
      } else {
        return Optional.empty();
      }
    } catch (NullPointerException e) {
      return Optional.empty();
    }
  }

  private Optional<String> checkDateTime(final String subject) {
    try {
      new SimpleDateFormat(DATETIME_FORMAT_STRING).parse(subject);
      return Optional.empty();
    } catch (ParseException e) {
      try {
        new SimpleDateFormat(DATETIME_FORMAT_STRING_SECFRAC).parse(subject);
        return Optional.empty();
      } catch (ParseException e1) {
        return Optional.of(String.format("[%s] is not a valid date-time", subject));
      }
    }
  }

  private Optional<String> checkEmail(final String subject) {
    if (EmailValidator.getInstance(false, true).isValid(subject)) {
      return Optional.empty();
    }
    return Optional.of(String.format("[%s] is not a valid email address", subject));
  }

  private Optional<String> checkHostname(final String subject) {
    try {
      InternetDomainName.from(subject);
      return Optional.empty();
    } catch (IllegalArgumentException | NullPointerException e) {
      return Optional.of(String.format("[%s] is not a valid hostname", subject));
    }
  }

  private Optional<String> checkIpAddress(final String subject, final int expectedLength,
      final String failureFormat) {
    return asInetAddress(subject)
        .filter(addr -> addr.getAddress().length == expectedLength)
        .map(addr -> Optional.<String> empty())
        .orElse(Optional.of(String.format(failureFormat, subject)));
  }

  private Optional<String> checkURI(final String subject) {
    try {
      new URI(subject);
      return Optional.empty();
    } catch (URISyntaxException | NullPointerException e) {
      return Optional.of(String.format("[%s] is not a valid URI", subject));
    }
  }

  @Override
  public Optional<String> validate(final String subject, final Format format) {
    Objects.requireNonNull(format, "format cannot be null");
    switch (format) {
      case IPV4:
        return checkIpAddress(subject, IPV4_LENGTH, "[%s] is not a valid ipv4 address");
      case IPV6:
        return checkIpAddress(subject, IPV6_LENGTH, "[%s] is not a valid ipv6 address");
      case HOSTNAME:
        return checkHostname(subject);
      case URI:
        return checkURI(subject);
      case DATE_TIME:
        return checkDateTime(subject);
      case EMAIL:
        return checkEmail(subject);
      default:
        throw new IllegalArgumentException("unsupported format: " + format);
    }
  }
}
