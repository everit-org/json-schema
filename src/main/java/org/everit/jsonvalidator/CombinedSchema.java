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
    if (matchingSubschemaCount < subschemaCount) {
      throw new ValidationException(String.format("only %d subschema matches out of %d",
          matchingSubschemaCount, subschemaCount));
    }
  };

  public static final ValidationCriterion ANY_CRITERION = (subschemaCount, matchingSubschemaCount) -> {
    if (matchingSubschemaCount == 0) {
      throw new ValidationException(String.format(
          "no subschema matched out of the total %d subschemas",
          subschemaCount));
    }
  };

  public static final ValidationCriterion ONE_CRITERION = (subschemaCount, matchingSubschemaCount) -> {
    if (matchingSubschemaCount != 1) {
      throw new ValidationException(String.format("%d subschemas matched instead of one",
          matchingSubschemaCount));
    }
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
