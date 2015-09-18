package org.everit.jsonvalidator;

import java.util.Arrays;

import org.junit.Test;

public class CombinedSchemaTest {

  @Test
  public void factories() {
    CombinedSchema.allOf(Arrays.asList(BooleanSchema.INSTANCE));
    CombinedSchema.anyOf(Arrays.asList(BooleanSchema.INSTANCE));
    CombinedSchema.oneOf(Arrays.asList(BooleanSchema.INSTANCE));
  }

}
