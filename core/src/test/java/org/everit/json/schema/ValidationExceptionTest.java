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

import org.junit.Assert;
import org.junit.Test;

public class ValidationExceptionTest {

  @Test
  public void constructorNullSchema() {
    new ValidationException(null, Boolean.class, 2);
  }

  @Test(expected = NullPointerException.class)
  public void nullPointerFragmentFailure() {
    new ValidationException(BooleanSchema.INSTANCE, Boolean.class, 2).prepend(null);
  }

  @Test
  public void prependPointer() {
    ValidationException exc = new ValidationException(BooleanSchema.INSTANCE, Boolean.class, 2);
    ValidationException changedExc = exc.prepend("frag");
    Assert.assertEquals("#/frag", changedExc.getPointerToViolation());
  }

  @Test
  public void testConstructor() {
    ValidationException exc = new ValidationException(BooleanSchema.INSTANCE, Boolean.class, 2);
    Assert.assertEquals("#", exc.getPointerToViolation());
  }

}
