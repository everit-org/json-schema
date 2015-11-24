package org.everit.json.schema;

import org.junit.Assert;

public class TestSupport {

  public static void exceptFailure(final Schema failingSchema, final Object input) {
    try {
      failingSchema.validate(input);
      Assert.fail(failingSchema + " did not fail for " + input);
    } catch (ValidationException e) {
      Assert.assertEquals(failingSchema, e.getViolatedSchema());
    }
  }

}
