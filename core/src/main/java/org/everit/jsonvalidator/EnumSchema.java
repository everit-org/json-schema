package org.everit.jsonvalidator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Enum schema.
 */
public class EnumSchema implements Schema {

  private final Set<Object> possibleValues;

  public EnumSchema(final Set<Object> possibleValues) {
    this.possibleValues = Collections.unmodifiableSet(new HashSet<>(possibleValues));
  }

  @Override
  public void validate(final Object subject) {
    possibleValues.stream()
        .filter(val -> ObjectComparator.deepEquals(val, subject))
        .findAny()
        .orElseThrow(
        () -> new ValidationException(String.format("%s is not a valid enum value",
            subject.toString())));
  }

  public Set<Object> getPossibleValues() {
    return possibleValues;
  }

}
