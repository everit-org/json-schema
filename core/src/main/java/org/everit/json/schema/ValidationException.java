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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Thrown by {@link Schema} subclasses on validation failure.
 */
public class ValidationException extends RuntimeException {
  private static final long serialVersionUID = 6192047123024651924L;

  /**
   * Sort of static factory method. It is used by {@link ObjectSchema} and {@link ArraySchema} to
   * create {@code ValidationException}s, handling the case of multiple violations occuring during
   * validation.
   *
   * <p>
   * <ul>
   * <li>If {@code failures} is empty, then it doesn't do anything</li>
   * <li>If {@code failures} contains 1 exception instance, then that will be thrown</li>
   * <li>Otherwise a new exception instance will be created, its {@link #getViolatedSchema()
   * violated schema} will be {@code rootFailingSchema}, and its {@link #getCausingExceptions()
   * causing exceptions} will be the {@code failures} list</li>
   * </ul>
   * </p>
   *
   */
  public static void throwFor(final Schema rootFailingSchema,
      final List<ValidationException> failures) {
    int failureCount = failures.size();
    if (failureCount == 0) {
      return;
    } else if (failureCount == 1) {
      throw failures.get(0);
    } else {
      throw new ValidationException(rootFailingSchema, new ArrayList<>(failures));
    }
  }

  private final StringBuilder pointerToViolation;

  private final transient Schema violatedSchema;

  private final List<ValidationException> causingExceptions;

  /**
   * Deprecated, use {@code ValidationException(Schema, Class<?>, Object)} instead.
   */
  @Deprecated
  public ValidationException(final Class<?> expectedType, final Object actualValue) {
    this(null, expectedType, actualValue);
  }

  /**
   * Constructor.
   */
  public ValidationException(final Schema violatedSchema, final Class<?> expectedType,
      final Object actualValue) {
    this(violatedSchema, new StringBuilder("#"),
        "expected type: " + expectedType.getSimpleName() + ", found: "
            + (actualValue == null ? "null" : actualValue.getClass().getSimpleName()),
        Collections.emptyList());
  }

  private ValidationException(final Schema rootFailingSchema,
      final List<ValidationException> causingExceptions) {
    this(rootFailingSchema, new StringBuilder("#"),
        causingExceptions.size() + " schema violations found",
        causingExceptions);
  }

  public ValidationException(final Schema violatedSchema, final String message) {
    this(violatedSchema, new StringBuilder("#"), message, Collections.emptyList());
  }

  /***
   * Constructor.
   */
  public ValidationException(final Schema violatedSchema, final StringBuilder pointerToViolation,
      final String message,
      final List<ValidationException> causingExceptions) {
    super(message);
    this.violatedSchema = violatedSchema;
    this.pointerToViolation = pointerToViolation;
    this.causingExceptions = Collections.unmodifiableList(causingExceptions);
  }

  /**
   * Deprecated, use {@code ValidationException(Schema, String)} instead.
   *
   * @param message
   *          readable exception message
   */
  @Deprecated
  public ValidationException(final String message) {
    this((Schema) null, new StringBuilder("#"), message, Collections.emptyList());
  }

  private ValidationException(final StringBuilder pointerToViolation,
      final Schema violatedSchema,
      final String message,
      final List<ValidationException> causingExceptions) {
    this(violatedSchema, pointerToViolation, message, causingExceptions);
  }

  public List<ValidationException> getCausingExceptions() {
    return causingExceptions;
  }

  public String getPointerToViolation() {
    return pointerToViolation.toString();
  }

  public Schema getViolatedSchema() {
    return violatedSchema;
  }

  /**
   * Creates a new {@code ViolationException} instance based on this one, but with changed
   * {@link #getPointerToViolation() JSON pointer}.
   *
   * @param fragment
   *          the fragment of the JSON pointer to be prepended to existing pointers
   * @return the new instance
   */
  public ValidationException prepend(final String fragment) {
    return prepend(fragment, this.violatedSchema);
  }

  /**
   * Creates a new {@code ViolationException} instance based on this one, but with changed
   * {@link #getPointerToViolation() JSON pointer} and {link {@link #getViolatedSchema() violated
   * schema}.
   *
   * @param fragment
   *          the fragment of the JSON pointer to be prepended to existing pointers
   * @param violatedSchema
   *          the violated schema, which may not be the same as {@link #getViolatedSchema()}
   * @return the new {@code ViolationException} instance
   */
  public ValidationException prepend(final String fragment, final Schema violatedSchema) {
    Objects.requireNonNull(fragment, "fragment cannot be null");
    StringBuilder newPointer = this.pointerToViolation.insert(1, '/').insert(2, fragment);
    List<ValidationException> prependedCausingExceptions = causingExceptions.stream()
        .map(exc -> exc.prepend(fragment))
        .collect(Collectors.toList());
    return new ValidationException(newPointer, violatedSchema, getMessage(),
        prependedCausingExceptions);
  }

}
