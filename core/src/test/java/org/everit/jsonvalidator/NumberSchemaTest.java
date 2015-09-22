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

import org.junit.Test;

public class NumberSchemaTest {

  @Test(expected = ValidationException.class)
  public void exclusiveMinimum() {
    NumberSchema.builder().minimum(10.0).exclusiveMinimum(true).build().validate(10);
  }

  @Test(expected = ValidationException.class)
  public void maximum() {
    NumberSchema.builder().maximum(20.0).build().validate(21);
  }

  @Test(expected = ValidationException.class)
  public void maximumExclusive() {
    NumberSchema.builder().maximum(20.0).exclusiveMaximum(true).build().validate(20);
  }

  @Test(expected = ValidationException.class)
  public void minimumFailure() {
    NumberSchema.builder().minimum(10.0).build().validate(9);
  }

  @Test(expected = ValidationException.class)
  public void multipleOfFailure() {
    NumberSchema.builder().multipleOf(10).build().validate(15);
  }

  @Test
  public void notRequiresNumber() {
    NumberSchema.builder().requiresNumber(false).build().validate("foo");
  }

  @Test(expected = ValidationException.class)
  public void requiresIntegerFailure() {
    NumberSchema.builder().requiresInteger(true).build().validate(new Float(10.2));
  }

  @Test
  public void requiresIntegerSuccess() {
    NumberSchema.builder().requiresInteger(true).build().validate(10);
  }

  @Test
  public void success() {
    NumberSchema.builder()
        .minimum(10.0)
        .maximum(11.0)
        .exclusiveMaximum(true)
        .multipleOf(10)
        .build().validate(10.0);
  }

  @Test(expected = ValidationException.class)
  public void typeFailure() {
    NumberSchema.builder().build().validate(null);
  }

}
