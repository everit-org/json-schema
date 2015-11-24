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
