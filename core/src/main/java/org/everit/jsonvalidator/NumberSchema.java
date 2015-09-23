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

import java.math.BigDecimal;

/**
 * Integer schema.
 */
public class NumberSchema implements Schema {

  /**
   * Builder class for {@link NumberSchema}.
   */
  public static class Builder {

    private Number minimum;

    private Number maximum;

    private Number multipleOf;

    private boolean exclusiveMinimum = false;

    private boolean exclusiveMaximum = false;

    private boolean requiresNumber = true;

    private boolean requiresInteger = false;

    public NumberSchema build() {
      return new NumberSchema(this);
    }

    public Builder exclusiveMaximum(final boolean exclusiveMaximum) {
      this.exclusiveMaximum = exclusiveMaximum;
      return this;
    }

    public Builder exclusiveMinimum(final boolean exclusiveMinimum) {
      this.exclusiveMinimum = exclusiveMinimum;
      return this;
    }

    public Builder maximum(final Number maximum) {
      this.maximum = maximum;
      return this;
    }

    public Builder minimum(final Number minimum) {
      this.minimum = minimum;
      return this;
    }

    public Builder multipleOf(final Number multipleOf) {
      this.multipleOf = multipleOf;
      return this;
    }

    public Builder requiresInteger(final boolean requiresInteger) {
      this.requiresInteger = requiresInteger;
      return this;
    }

    public Builder requiresNumber(final boolean requiresNumber) {
      this.requiresNumber = requiresNumber;
      return this;
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  private final boolean requiresNumber;

  private final Number minimum;

  private final Number maximum;

  private final Number multipleOf;

  private boolean exclusiveMinimum = false;

  private boolean exclusiveMaximum = false;

  private final boolean requiresInteger;

  public NumberSchema() {
    this(builder());
  }

  /**
   * Constructor.
   */
  public NumberSchema(final Builder builder) {
    this.minimum = builder.minimum;
    this.maximum = builder.maximum;
    this.exclusiveMinimum = builder.exclusiveMinimum;
    this.exclusiveMaximum = builder.exclusiveMaximum;
    this.multipleOf = builder.multipleOf;
    this.requiresNumber = builder.requiresNumber;
    this.requiresInteger = builder.requiresInteger;
  }

  private void checkMaximum(final double subject) {
    if (maximum != null) {
      if (exclusiveMaximum && maximum.doubleValue() <= subject) {
        throw new ValidationException(subject + " is not lower than " + maximum);
      } else if (maximum.doubleValue() < subject) {
        throw new ValidationException(subject + " is not lower or equal to " + maximum);
      }
    }
  }

  private void checkMinimum(final double subject) {
    if (minimum != null) {
      if (exclusiveMinimum && subject <= minimum.doubleValue()) {
        throw new ValidationException(subject + " is not higher than " + minimum);
      } else if (subject < minimum.doubleValue()) {
        throw new ValidationException(subject + " is not higher or equal to " + minimum);
      }
    }
  }

  private void checkMultipleOf(final double subject) {
    if (multipleOf != null) {
      BigDecimal remainder = BigDecimal.valueOf(subject).remainder(
          BigDecimal.valueOf(multipleOf.doubleValue()));
      if (remainder.compareTo(BigDecimal.ZERO) != 0) {
        throw new ValidationException(subject + " is not a multiple of " + multipleOf);
      }
    }
  }

  public Number getMaximum() {
    return maximum;
  }

  public Number getMinimum() {
    return minimum;
  }

  public Number getMultipleOf() {
    return multipleOf;
  }

  public boolean isExclusiveMaximum() {
    return exclusiveMaximum;
  }

  public boolean isExclusiveMinimum() {
    return exclusiveMinimum;
  }

  public boolean requiresInteger() {
    return requiresInteger;
  }

  @Override
  public void validate(final Object subject) {
    if (!(subject instanceof Number)) {
      if (requiresNumber) {
        throw new ValidationException(Number.class, subject);
      }
    } else {
      if (!(subject instanceof Integer) && requiresInteger) {
        throw new ValidationException(Integer.class, subject);
      }
      double intSubject = ((Number) subject).doubleValue();
      checkMinimum(intSubject);
      checkMaximum(intSubject);
      checkMultipleOf(intSubject);
    }
  }

}
