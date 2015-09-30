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

import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.junit.Test;

public class StringSchemaTest {

  @Test(expected = ValidationException.class)
  public void maxLength() {
    StringSchema.builder().maxLength(3).build().validate("foobar");
  }

  @Test(expected = ValidationException.class)
  public void minLength() {
    StringSchema.builder().minLength(2).build().validate("a");
  }

  @Test
  public void success() {
    StringSchema.builder().build().validate("foo");
  }

  @Test(expected = ValidationException.class)
  public void typeFailure() {
    StringSchema.builder().build().validate(null);
  }

  @Test
  public void patternSuccess() {
    StringSchema.builder().pattern("^a*$").build().validate("aaaa");
  }

  @Test(expected = ValidationException.class)
  public void patternFailure() {
    StringSchema.builder().pattern("^a*$").build().validate("abc");
  }

  @Test
  public void notRequiresString() {
    StringSchema.builder().requiresString(false).build().validate(2);
  }
}
