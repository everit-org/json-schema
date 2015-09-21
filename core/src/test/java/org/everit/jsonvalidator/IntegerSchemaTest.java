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

public class IntegerSchemaTest {

  @Test(expected = ValidationException.class)
  public void exclusiveMinimum() {
    IntegerSchema.builder().minimum(10).exclusiveMinimum(true).build().validate(10);
  }

  @Test(expected = ValidationException.class)
  public void maximum() {
    IntegerSchema.builder().maximum(20).build().validate(21);
  }

  @Test(expected = ValidationException.class)
  public void maximumExclusive() {
    IntegerSchema.builder().maximum(20).exclusiveMaximum(true).build().validate(20);
  }

  @Test(expected = ValidationException.class)
  public void minimumFailure() {
    IntegerSchema.builder().minimum(10).build().validate(9);
  }

  @Test(expected = ValidationException.class)
  public void multipleOfFailure() {
    IntegerSchema.builder().multipleOf(10).build().validate(15);
  }

  @Test
  public void success() {
    IntegerSchema.builder()
        .minimum(10)
        .maximum(11)
        .exclusiveMaximum(true)
        .multipleOf(10)
        .build().validate(10);
  }

  @Test(expected = ValidationException.class)
  public void typeFailure() {
    IntegerSchema.builder().build().validate(null);
  }

}
