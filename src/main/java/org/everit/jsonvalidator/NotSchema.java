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

import java.util.Objects;

public class NotSchema implements Schema {

  private final Schema mustNotMatch;

  public NotSchema(final Schema mustNotMatch) {
    this.mustNotMatch = Objects.requireNonNull(mustNotMatch, "mustNotMatch cannot be null");
  }

  @Override
  public void validate(final Object subject) {
    try {
      mustNotMatch.validate(subject);
    } catch (ValidationException e) {
      return;
    }
    throw new ValidationException("subject must not be valid agains schema " + mustNotMatch);
  }
}
