package org.everit.jsonvalidator;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

public class ObjectSchemaTest {

  private static final JSONObject OBJECTS = new JSONObject(new JSONTokener(
      ObjectSchemaTest.class
      .getResourceAsStream("/org/everit/jsonvalidator/objecttestcases.json")));

  @Test(expected = SchemaException.class)
  public void schemaForNoAdditionalProperties() {
    ObjectSchema.builder().additionalProperties(false)
        .schemaOfAdditionalProperties(BooleanSchema.INSTANCE).build();
  }

  @Test(expected = ValidationException.class)
  public void propertySchemaViolation() {
    ObjectSchema.builder().addPropertySchema("boolProp", BooleanSchema.INSTANCE).build()
        .validate(OBJECTS.get("propertySchemaViolation"));
  }

  @Test(expected = ValidationException.class)
  public void typeFailure() {
    ObjectSchema.builder().build().validate("a");
  }

  @Test
  public void noProperties() {
    ObjectSchema.builder().build().validate(OBJECTS.get("noProperties"));
  }

  @Test(expected = ValidationException.class)
  public void requiredProperties() {
    ObjectSchema.builder().
    addPropertySchema("boolProp", BooleanSchema.INSTANCE)
    .addPropertySchema("nullProp", NullSchema.INSTANCE)
    .addRequiredProperty("boolProp")
    .build().validate(OBJECTS.get("requiredProperties"));
  }

  @Test(expected = ValidationException.class)
  public void noAdditionalProperties() {
    ObjectSchema.builder().additionalProperties(false).build()
        .validate(OBJECTS.get("propertySchemaViolation"));
  }

  @Test(expected = ValidationException.class)
  public void minPropertiesFailure() {
    ObjectSchema.builder().minProperties(2).build().validate(OBJECTS.get("minPropertiesFailure"));
  }

  @Test(expected = ValidationException.class)
  public void maxPropertiesFailure() {
    ObjectSchema.builder().maxProperties(2).build().validate(OBJECTS.get("maxPropertiesFailure"));
  }

}
