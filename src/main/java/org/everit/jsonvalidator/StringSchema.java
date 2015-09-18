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

/**
 * Javadoc.
 */
public class StringSchema implements Schema {

  private final Integer minLength;

  private final Integer maxLength;

  private final String pattern;

  public StringSchema() {
    this(null, null, null);
  }

  public StringSchema(final Integer minLength, final Integer maxLength, final String pattern) {
    this.minLength = minLength;
    this.maxLength = maxLength;
    this.pattern = pattern;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public Integer getMinLength() {
    return minLength;
  }

  public String getPattern() {
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

  @Override
  public void validate(final Object subject) {
    if (!(subject instanceof String)) {
      throw new ValidationException(String.class, subject);
    }
    String stringSubject = (String) subject;
    testLength(stringSubject);
  }

}
