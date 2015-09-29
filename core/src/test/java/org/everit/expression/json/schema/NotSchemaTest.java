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
package org.everit.expression.json.schema;

import org.everit.expression.json.schema.BooleanSchema;
import org.everit.expression.json.schema.NotSchema;
import org.everit.expression.json.schema.ValidationException;
import org.junit.Test;

public class NotSchemaTest {

  @Test
  public void success() {
    NotSchema.builder().mustNotMatch(BooleanSchema.INSTANCE).build().validate("foo");
  }

  @Test(expected = ValidationException.class)
  public void failure() {
    NotSchema.builder().mustNotMatch(BooleanSchema.INSTANCE).build().validate(true);
  }

}
