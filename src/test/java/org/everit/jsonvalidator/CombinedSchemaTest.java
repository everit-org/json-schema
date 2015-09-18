package org.everit.jsonvalidator;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CombinedSchemaTest {

  @Test
  public void factories() {
    CombinedSchema.allOf(Arrays.asList(BooleanSchema.INSTANCE));
    CombinedSchema.anyOf(Arrays.asList(BooleanSchema.INSTANCE));
    CombinedSchema.oneOf(Arrays.asList(BooleanSchema.INSTANCE));
  }

  @Test
  public void allCriterionSuccess() {
    CombinedSchema.ALL_CRITERION.validate(10, 10);
  }

  @Test(expected = ValidationException.class)
  public void allCriterionFailure() {
    CombinedSchema.ALL_CRITERION.validate(10, 1);
  }

  @Test
  public void anyCriterionSuccess() {
    CombinedSchema.ANY_CRITERION.validate(10, 1);
  }

  @Test(expected = ValidationException.class)
  public void anyCriterionFailure() {
    CombinedSchema.ANY_CRITERION.validate(10, 0);
  }

  @Test
  public void oneCriterionSuccess() {
    CombinedSchema.ONE_CRITERION.validate(10, 1);
  }

  @Test(expected = ValidationException.class)
  public void oneCriterionFailure() {
    CombinedSchema.ONE_CRITERION.validate(10, 2);
  }

  private static final List<Schema> SUBSCHEMAS = Arrays.asList(
      IntegerSchema.builder().multipleOf(10).build(),
      IntegerSchema.builder().multipleOf(3).build()
      );

  @Test(expected = ValidationException.class)
  public void validateAll() {
    CombinedSchema.allOf(SUBSCHEMAS)
    .validate(20);
  }

  @Test(expected = ValidationException.class)
  public void validateAny() {
    CombinedSchema.anyOf(SUBSCHEMAS)
    .validate(5);
  }

  @Test(expected = ValidationException.class)
  public void validateOne() {
    CombinedSchema.oneOf(SUBSCHEMAS)
    .validate(30);
  }
}
