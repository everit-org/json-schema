package org.everit.json.schema;

import java.util.Optional;

import org.everit.json.schema.internal.DefaultFormatValidator;

@FunctionalInterface
public interface FormatValidator {

  /**
   * No-operation implementation (never throws {@link ValidationException}).
   */
  public static final FormatValidator NONE = (subject, format) -> Optional.empty();

  public static FormatValidator DEFAULT = new DefaultFormatValidator();

  Optional<String> validate(String subject, Format format);

}
