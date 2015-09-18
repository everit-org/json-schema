package org.everit.jsonvalidator;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class CombinedSchema implements Schema {

  @FunctionalInterface
  public interface ValidationCriterion {

    /**
     * Throws a {@link ValidationException} if the implemented criterion is not fulfilled by the
     * {@code subschemaCount} and the {@code matchingSubschemaCount}.
     */
    void validate(int subschemaCount, int matchingSubschemaCount);

  }

  public static final ValidationCriterion ALL_CRITERION = (subschemaCount, matchingSubschemaCount) -> {

  };

  public static final ValidationCriterion ANY_CRITERION = (subschemaCount, matchingSubschemaCount) -> {

  };

  public static final ValidationCriterion ONE_CRITERION = (subschemaCount, matchingSubschemaCount) -> {

  };

  public static CombinedSchema allOf(final Collection<Schema> schemas) {
    return new CombinedSchema(schemas, ALL_CRITERION);
  }

  public static CombinedSchema anyOf(final Collection<Schema> schemas) {
    return new CombinedSchema(schemas, ANY_CRITERION);
  }

  public static CombinedSchema oneOf(final Collection<Schema> schemas) {
    return new CombinedSchema(schemas, ONE_CRITERION);
  }

  private final Collection<Schema> subschemas;

  private final ValidationCriterion criterion;

  public CombinedSchema(final Collection<Schema> subschemas, final ValidationCriterion criterion) {
    this.subschemas = Collections.unmodifiableCollection(subschemas);
    this.criterion = Objects.requireNonNull(criterion, "criterion cannot be null");
  }

  @Override
  public void validate(final Object subject) {
    // TODO Auto-generated method stub

  }

}
