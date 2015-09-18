package org.everit.jsonvalidator;

import org.junit.Test;

public class NotSchemaTest {

  @Test
  public void success() {
    new NotSchema(BooleanSchema.INSTANCE).validate("foo");
  }

  @Test(expected = ValidationException.class)
  public void failure() {
    new NotSchema(BooleanSchema.INSTANCE).validate(true);
  }

}
