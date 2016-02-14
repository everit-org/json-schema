package org.everit.json.schema;

import org.everit.json.schema.internal.DefaultFormatValidator;

@FunctionalInterface
public interface FormatValidator {

  /**
   * No-operation implementation (never throws {@link ValidationException}).
   */
  public static final FormatValidator NONE = (subject, format) -> {
  };

  public static FormatValidator DEFAULT = new DefaultFormatValidator();

  void validate(String subject, Format format);

}
