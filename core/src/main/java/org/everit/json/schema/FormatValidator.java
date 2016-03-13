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
package org.everit.json.schema;

import java.util.Objects;
import java.util.Optional;

import org.everit.json.schema.internal.DateTimeFormatValidator;
import org.everit.json.schema.internal.EmailFormatValidator;
import org.everit.json.schema.internal.HostnameFormatValidator;
import org.everit.json.schema.internal.IPV4Validator;
import org.everit.json.schema.internal.IPV6Validator;
import org.everit.json.schema.internal.URIFormatValidator;

/**
 * Implementations perform the validation against the "format" keyword (see JSON Schema spec section
 * 7).
 */
@FunctionalInterface
public interface FormatValidator {

  /**
   * No-operation implementation (never throws {always returns {@link Optional#empty()}).
   */
  FormatValidator NONE = subject -> Optional.empty();

  /**
   * Static factory method for {@code FormatValidator} implementations supporting the
   * {@code formatName}s mandated by the json schema spec.
   *
   * <ul>
   * <li>date-time</li>
   * <li>email</li>
   * <li>hostname</li>
   * <li>uri</li>
   * <li>ipv4</li>
   * <li>ipv6</li>
   * </ul>
   *
   * @param formatName
   *          one of the 6 built-in formats.
   * @return a {@code FormatValidator} implementation handling the {@code formatName} format.
   */
  static FormatValidator forFormat(final String formatName) {
    Objects.requireNonNull(formatName, "formatName cannot be null");
    switch (formatName) {
      case "date-time":
        return new DateTimeFormatValidator();
      case "email":
        return new EmailFormatValidator();
      case "hostname":
        return new HostnameFormatValidator();
      case "uri":
        return new URIFormatValidator();
      case "ipv4":
        return new IPV4Validator();
      case "ipv6":
        return new IPV6Validator();
      default:
        throw new IllegalArgumentException("unsupported format: " + formatName);
    }
  }

  Optional<String> validate(String subject);

}
