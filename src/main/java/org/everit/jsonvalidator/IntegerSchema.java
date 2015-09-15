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

public class IntegerSchema implements Schema {

  public static class Builder {

    private Integer minimum;

    private Integer maximum;

    private Integer multipleOf;

    private boolean exclusiveMinimum = false;

    private boolean exclusiveMaximum = false;

    public IntegerSchema build() {
      return new IntegerSchema(this);
    }

    public Builder exclusiveMaximum(final boolean exclusiveMaximum) {
      this.exclusiveMaximum = exclusiveMaximum;
      return this;
    }

    public Builder exclusiveMinimum(final boolean exclusiveMinimum) {
      this.exclusiveMinimum = exclusiveMinimum;
      return this;
    }

    public Builder maximum(final Integer maximum) {
      this.maximum = maximum;
      return this;
    }

    public Builder minimum(final Integer minimum) {
      this.minimum = minimum;
      return this;
    }

    public Builder multipleOf(final Integer multipleOf) {
      this.multipleOf = multipleOf;
      return this;
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  private final Integer minimum;

  private final Integer maximum;

  private final Integer multipleOf;

  private boolean exclusiveMinimum = false;

  private boolean exclusiveMaximum = false;

  public IntegerSchema(final Builder builder) {
    this.minimum = builder.minimum;
    this.maximum = builder.maximum;
    this.exclusiveMinimum = builder.exclusiveMinimum;
    this.exclusiveMaximum = builder.exclusiveMaximum;
    this.multipleOf = builder.multipleOf;
  }

  private void checkMaximum(final int subject) {
    if (maximum != null) {
      if (exclusiveMaximum && maximum <= subject) {
        throw new ValidationException(subject + " is not lower than " + maximum);
      } else if (maximum < subject) {
        throw new ValidationException(subject + " is not lower or equal to " + maximum);
      }
    }
  }

  private void checkMinimum(final int subject) {
    if (minimum != null) {
      if (exclusiveMinimum && subject <= minimum) {
        throw new ValidationException(subject + " is not higher than " + minimum);
      } else if (subject < minimum) {
        throw new ValidationException(subject + " is not higher or equal to " + minimum);
      }
    }
  }

  private void checkMultipleOf(final int intSubject) {
    if (multipleOf != null) {
      if (intSubject % multipleOf != 0) {
        throw new ValidationException(intSubject + " is not a multiple of " + multipleOf);
      }
    }
  }

  public Integer getMaximum() {
    return maximum;
  }

  public Integer getMinimum() {
    return minimum;
  }

  public Integer getMultipleOf() {
    return multipleOf;
  }

  public boolean isExclusiveMaximum() {
    return exclusiveMaximum;
  }

  public boolean isExclusiveMinimum() {
    return exclusiveMinimum;
  }

  public void setExclusiveMaximum(final boolean exclusiveMaximum) {
    this.exclusiveMaximum = exclusiveMaximum;
  }

  public void setExclusiveMinimum(final boolean exclusiveMinimum) {
    this.exclusiveMinimum = exclusiveMinimum;
  }

  @Override
  public void validate(final Object subject) {
    if (!(subject instanceof Integer)) {
      throw new ValidationException(Integer.class, subject);
    }
    int intSubject = (Integer) subject;
    checkMinimum(intSubject);
    checkMaximum(intSubject);
    checkMultipleOf(intSubject);
  }

}
