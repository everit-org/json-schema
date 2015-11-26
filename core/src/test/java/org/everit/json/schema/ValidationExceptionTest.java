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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ValidationExceptionTest {

  private final Schema rootSchema = ObjectSchema.builder().build();

  @Test
  public void constructorNullSchema() {
    new ValidationException(null, Boolean.class, 2);
  }

  private ValidationException createDummyException(final String pointer) {
    return new ValidationException(BooleanSchema.INSTANCE,
        new StringBuilder(pointer),
        "stuff went wrong", Collections.emptyList());
  }

  @Test(expected = NullPointerException.class)
  public void nullPointerFragmentFailure() {
    new ValidationException(BooleanSchema.INSTANCE, Boolean.class, 2).prepend(null,
        NullSchema.INSTANCE);
  }

  @Test
  public void prependNoSchemaChange() {
    ValidationException exc = new ValidationException(BooleanSchema.INSTANCE, Boolean.class, 2);
    ValidationException changedExc = exc.prepend("frag");
    Assert.assertEquals("#/frag", changedExc.getPointerToViolation());
    Assert.assertEquals(BooleanSchema.INSTANCE, changedExc.getViolatedSchema());
  }

  @Test
  public void prependPointer() {
    ValidationException exc = new ValidationException(BooleanSchema.INSTANCE, Boolean.class, 2);
    ValidationException changedExc = exc.prepend("frag", NullSchema.INSTANCE);
    Assert.assertEquals("#/frag", changedExc.getPointerToViolation());
    Assert.assertEquals(NullSchema.INSTANCE, changedExc.getViolatedSchema());
  }

  @Test
  public void prependWithCausingExceptions() {
    ValidationException cause1 = createDummyException("#/a");
    ValidationException cause2 = createDummyException("#/b");
    try {
      ValidationException.throwFor(rootSchema, Arrays.asList(cause1, cause2));
      Assert.fail();
    } catch (ValidationException e) {
      ValidationException actual = e.prepend("rectangle");
      Assert.assertEquals("#/rectangle", actual.getPointerToViolation());
      ValidationException changedCause1 = actual.getCausingExceptions().get(0);
      Assert.assertEquals("#/rectangle/a", changedCause1.getPointerToViolation());
      ValidationException changedCause2 = actual.getCausingExceptions().get(1);
      Assert.assertEquals("#/rectangle/b", changedCause2.getPointerToViolation());
    }

  }

  @Test
  public void testConstructor() {
    ValidationException exc = new ValidationException(BooleanSchema.INSTANCE, Boolean.class, 2);
    Assert.assertEquals("#", exc.getPointerToViolation());
  }

  @Test
  public void throwForMultipleFailures() {
    ValidationException input1 = new ValidationException(NullSchema.INSTANCE, "msg1");
    ValidationException input2 = new ValidationException(BooleanSchema.INSTANCE, "msg2");
    try {
      ValidationException.throwFor(rootSchema, Arrays.asList(input1, input2));
      Assert.fail("did not throw exception for 2 input exceptions");
    } catch (ValidationException e) {
      Assert.assertSame(rootSchema, e.getViolatedSchema());
      Assert.assertEquals("2 schema violations found", e.getMessage());
      List<ValidationException> causes = e.getCausingExceptions();
      Assert.assertEquals(2, causes.size());
      Assert.assertSame(input1, causes.get(0));
      Assert.assertSame(input2, causes.get(1));
    }
  }

  @Test
  public void throwForNoFailure() {
    ValidationException.throwFor(rootSchema, Collections.emptyList());
  }

  @Test
  public void throwForSingleFailure() {
    ValidationException input = new ValidationException(NullSchema.INSTANCE, "msg");
    try {
      ValidationException.throwFor(rootSchema, Arrays.asList(input));
      Assert.fail("did not throw exception for single failure");
    } catch (ValidationException actual) {
      Assert.assertSame(input, actual);
    }
  }

}
