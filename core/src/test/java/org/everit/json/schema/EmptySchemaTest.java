package org.everit.json.schema;

import org.junit.Assert;
import org.junit.Test;

public class EmptySchemaTest {

  @Test
  public void testValidate() {
    EmptySchema.INSTANCE.validate("something");
  }

  @Test
  public void testBuilder() {
    Assert.assertSame(EmptySchema.builder().build(), EmptySchema.builder().build());
  }

  @Test
  public void testToString() {
    Assert.assertEquals("{}", EmptySchema.INSTANCE.toString());
  }

}
