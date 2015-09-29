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
package org.everit.expression.json.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Validator for {@code allOf}, {@code oneOf}, {@code anyOf} schemas.
 */
public class CombinedSchema extends Schema {

  /**
   * Builder class for {@link CombinedSchema}.
   */
  public static class Builder extends Schema.Builder<CombinedSchema> {

    private ValidationCriterion criterion;

    private Collection<Schema> subschemas = new ArrayList<>();

    public Builder criterion(final ValidationCriterion criterion) {
      this.criterion = criterion;
      return this;
    }

    public Builder subschema(final Schema subschema) {
      this.subschemas.add(subschema);
      return this;
    }

    public Builder subschemas(final Collection<Schema> subschemas) {
      this.subschemas = subschemas;
      return this;
    }

    @Override
    public CombinedSchema build() {
      return new CombinedSchema(this);
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(final Collection<Schema> subschemas) {
    return new Builder().subschemas(subschemas);
  }

  /**
   * Validation criterion.
   */
  @FunctionalInterface
  public interface ValidationCriterion {

    /**
     * Throws a {@link ValidationException} if the implemented criterion is not fulfilled by the
     * {@code subschemaCount} and the {@code matchingSubschemaCount}.
     */
    void validate(int subschemaCount, int matchingSubschemaCount);

  }

  /**
   * Validation criterion for {@code allOf} schemas.
   */
  public static final ValidationCriterion ALL_CRITERION = (subschemaCount, matchingCount) -> {
    if (matchingCount < subschemaCount) {
      throw new ValidationException(String.format("only %d subschema matches out of %d",
          matchingCount, subschemaCount));
    }
  };

  /**
   * Validation criterion for {@code anyOf} schemas.
   */
  public static final ValidationCriterion ANY_CRITERION = (subschemaCount, matchingCount) -> {
    if (matchingCount == 0) {
      throw new ValidationException(String.format(
          "no subschema matched out of the total %d subschemas",
          subschemaCount));
    }
  };

  /**
   * Validation criterion for {@code oneOf} schemas.
   */
  public static final ValidationCriterion ONE_CRITERION = (subschemaCount, matchingCount) -> {
    if (matchingCount != 1) {
      throw new ValidationException(String.format("%d subschemas matched instead of one",
          matchingCount));
    }
  };

  public static Builder allOf(final Collection<Schema> schemas) {
    return builder(schemas).criterion(ALL_CRITERION);
  }

  public static Builder anyOf(final Collection<Schema> schemas) {
    return builder(schemas).criterion(ANY_CRITERION);
  }

  public static Builder oneOf(final Collection<Schema> schemas) {
    return builder(schemas).criterion(ONE_CRITERION);
  }

  private final Collection<Schema> subschemas;

  private final ValidationCriterion criterion;

  /**
   * Constructor.
   */
  public CombinedSchema(final Builder builder) {
    super(builder);
    this.criterion = Objects.requireNonNull(builder.criterion, "criterion cannot be null");
    this.subschemas = Objects.requireNonNull(builder.subschemas, "subschemas cannot be null");
  }

  public ValidationCriterion getCriterion() {
    return criterion;
  }

  public Collection<Schema> getSubschemas() {
    return subschemas;
  }

  private boolean succeeds(final Schema schema, final Object subject) {
    try {
      schema.validate(subject);
      return true;
    } catch (ValidationException e) {
      return false;
    }
  }

  @Override
  public void validate(final Object subject) {
    int matchingCount = (int) subschemas.stream()
        .filter(schema -> succeeds(schema, subject))
        .count();
    criterion.validate(subschemas.size(), matchingCount);
  }

}
