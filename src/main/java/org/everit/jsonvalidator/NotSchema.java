package org.everit.jsonvalidator;

import java.util.Objects;

public class NotSchema implements Schema {

  private final Schema mustNotMatch;

  public NotSchema(final Schema mustNotMatch) {
    this.mustNotMatch = Objects.requireNonNull(mustNotMatch, "mustNotMatch cannot be null");
  }

  @Override
  public void validate(final Object subject) {
    try {
      mustNotMatch.validate(subject);
    } catch (ValidationException e) {
      return;
    }
    throw new ValidationException("subject must not be valid agains schema " + mustNotMatch);
  }
}
