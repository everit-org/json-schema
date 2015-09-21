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

public class StringSchemaTest {

  @Test(expected = ValidationException.class)
  public void maxLength() {
    new StringSchema(null, 3, null).validate("foobar");
  }

  @Test(expected = ValidationException.class)
  public void minLength() {
    new StringSchema(2, null, null).validate("a");
  }

  @Test
  public void success() {
    new StringSchema(null, null, null).validate("foo");
  }

  @Test(expected = ValidationException.class)
  public void typeFailure() {
    new StringSchema(null, null, null).validate(null);
  }

}
