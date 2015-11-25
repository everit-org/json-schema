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
package org.everit.json.schema;

import java.util.Objects;

/**
 * Thrown by {@link Schema} subclasses on validation failure.
 */
public class ValidationException extends RuntimeException {
  private static final long serialVersionUID = 6192047123024651924L;

  private final StringBuilder pointerToViolation;

  private final Schema violatedSchema;

  /**
   * Deprecated, use {@code ValidationException(Schema, Class<?>, Object)} instead.
   *
   * @param expectedType
   * @param actualValue
   */
  @Deprecated
  public ValidationException(final Class<?> expectedType, final Object actualValue) {
    this(null, expectedType, actualValue);
  }

  public ValidationException(final Schema violatedSchema, final Class<?> expectedType,
      final Object actualValue) {
    this(violatedSchema, "expected type: " + expectedType.getSimpleName() + ", found: "
        + (actualValue == null ? "null" : actualValue.getClass().getSimpleName()));
  }

  public ValidationException(final Schema violatedSchema, final String message) {
    super(message);
    this.violatedSchema = violatedSchema;
    this.pointerToViolation = new StringBuilder("#");
  }

  /**
   * Deprecated, use {@code ValidationException(Schema, String)} instead.
   *
   * @param message
   *          readable exception message
   */
  @Deprecated
  public ValidationException(final String message) {
    this((Schema) null, message);
  }

  private ValidationException(final StringBuilder pointerToViolation,
      final Schema violatedSchema,
      final ValidationException original) {
    super(original.getMessage());
    this.violatedSchema = violatedSchema;
    this.pointerToViolation = pointerToViolation;
  }

  public String getPointerToViolation() {
    return pointerToViolation.toString();
  }

  public Schema getViolatedSchema() {
    return violatedSchema;
  }

  public ValidationException prepend(final String fragment, final Schema violatingSchema) {
    Objects.requireNonNull(fragment, "fragment cannot be null");
    return new ValidationException(this.pointerToViolation.insert(1, '/').insert(2, fragment),
        violatingSchema, this);
  }

}
