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

import java.util.regex.Pattern;

/**
 * {@code String} schema validator.
 *
 * {@link http://json-schema.org/latest/json-schema-validation.html#anchor25 See the according
 * specification}.
 */
public class StringSchema extends Schema {

  /**
   * Builder class for {@link StringSchema}.
   */
  public static class Builder extends Schema.Builder {

    private Integer minLength;

    private Integer maxLength;

    private String pattern;

    private boolean requiresString = true;

    @Override
    public StringSchema build() {
      return new StringSchema(this);
    }

    public Builder maxLength(final Integer maxLength) {
      this.maxLength = maxLength;
      return this;
    }

    public Builder minLength(final Integer minLength) {
      this.minLength = minLength;
      return this;
    }

    public Builder pattern(final String pattern) {
      this.pattern = pattern;
      return this;
    }

    public Builder requiresString(final boolean requiresString) {
      this.requiresString = requiresString;
      return this;
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  private final Integer minLength;

  private final Integer maxLength;

  private final Pattern pattern;

  private final boolean requiresString;

  public StringSchema() {
    this(builder());
  }

  /**
   * Constructor.
   */
  public StringSchema(final Builder builder) {
    super(builder);
    this.minLength = builder.minLength;
    this.maxLength = builder.maxLength;
    this.requiresString = builder.requiresString;
    if (builder.pattern != null) {
      this.pattern = Pattern.compile(builder.pattern);
    } else {
      this.pattern = null;
    }
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public Integer getMinLength() {
    return minLength;
  }

  public Pattern getPattern() {
    return pattern;
  }

  private void testLength(final String subject) {
    int actualLength = subject.length();
    if (minLength != null && actualLength < minLength.intValue()) {
      throw new ValidationException("expected minLength: " + minLength + ", actual: "
          + actualLength);
    }
    if (maxLength != null && actualLength > maxLength.intValue()) {
      throw new ValidationException("expected maxLength: " + maxLength + ", actual: "
          + actualLength);
    }
  }

  private void testPattern(final String subject) {
    if (pattern != null && !pattern.matcher(subject).find()) {
      throw new ValidationException(String.format("string [%s] does not match pattern %s",
          subject, pattern.pattern()));
    }
  }

  @Override
  public void validate(final Object subject) {
    if (!(subject instanceof String)) {
      if (requiresString) {
        throw new ValidationException(String.class, subject);
      }
    } else {
      String stringSubject = (String) subject;
      testLength(stringSubject);
      testPattern(stringSubject);
    }
  }

}
