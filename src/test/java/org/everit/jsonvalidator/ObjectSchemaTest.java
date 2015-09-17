package org.everit.jsonvalidator;

import org.junit.Test;

public class ObjectSchemaTest {

  @Test(expected = SchemaException.class)
  public void schemaForNoAdditionalProperties() {
    ObjectSchema.builder().additionalProperties(false)
    .schemaOfAdditionalProperties(BooleanSchema.INSTANCE).build();
  }

}
