package org.everit.jsonvalidator;

import org.everit.jsonvalidator.ReferenceSchema.Builder;
import org.junit.Assert;
import org.junit.Test;

public class ReferenceSchemaTest {

  @Test
  public void constructorMustRunOnlyOnce() {
    Builder builder = ReferenceSchema.builder();
    Assert.assertSame(builder.build(), builder.build());
  }

  @Test(expected = IllegalStateException.class)
  public void setterShouldWorkOnlyOnce() {
    ReferenceSchema subject = ReferenceSchema.builder().build();
    subject.setReferredSchema(BooleanSchema.INSTANCE);
    subject.setReferredSchema(BooleanSchema.INSTANCE);
  }

}
