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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Enum schema.
 */
public class EnumSchema implements Schema {

  private final Set<Object> possibleValues;

  public EnumSchema(final Set<Object> possibleValues) {
    this.possibleValues = Collections.unmodifiableSet(new HashSet<>(possibleValues));
  }

  @Override
  public void validate(final Object subject) {
    possibleValues.stream()
        .filter(val -> ObjectComparator.deepEquals(val, subject))
        .findAny()
        .orElseThrow(
        () -> new ValidationException(String.format("%s is not a valid enum value",
            subject.toString())));
  }

  public Set<Object> getPossibleValues() {
    return possibleValues;
  }

}
