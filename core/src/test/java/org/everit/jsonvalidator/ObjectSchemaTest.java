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

  @Test(expected = ValidationException.class)
  public void propertyDepViolation() {
    ObjectSchema.builder()
    .addPropertySchema("ifPresent", NullSchema.INSTANCE)
    .addPropertySchema("mustBePresent", BooleanSchema.INSTANCE)
    .propertyDependency("ifPresent", "mustBePresent")
    .build().validate(OBJECTS.get("propertyDepViolation"));
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

  @Test(expected = ValidationException.class)
  public void schemaDepViolation() {
    ObjectSchema schema = ObjectSchema.builder()
        .addPropertySchema("name", new StringSchema())
        .addPropertySchema("credit_card", NumberSchema.builder().build())
        .schemaDependency("credit_card", ObjectSchema.builder()
            .addPropertySchema("billing_address", new StringSchema())
            .addRequiredProperty("billing_address")
            .build()).build();
    schema.validate(OBJECTS.get("schemaDepViolation"));
  }

  @Test
  public void notRequireObject() {
    ObjectSchema.builder().requiresObject(false).build().validate("foo");
  }

  @Test(expected = ValidationException.class)
  public void patternPropertyViolation() {
    ObjectSchema.builder()
        .patternProperty("b_.*", BooleanSchema.INSTANCE)
        .patternProperty("s_.*", new StringSchema())
        .build().validate(OBJECTS.get("patternPropertyViolation"));
  }

  @Test
  public void patternPropertyOnEmptyObjct() {
    ObjectSchema.builder()
    .patternProperty("b_.*", BooleanSchema.INSTANCE)
    .build().validate(new JSONObject());
  }

  @Test(expected = ValidationException.class)
  public void additionalPropertySchema() {
    ObjectSchema.builder()
    .schemaOfAdditionalProperties(BooleanSchema.INSTANCE)
    .build().validate(OBJECTS.get("additionalPropertySchema"));
  }

  @Test
  public void patternPropsOverrideAdditionalProps() {
    ObjectSchema.builder()
    .patternProperty("^v.*", EmptySchema.INSTANCE)
    .additionalProperties(false)
    .build().validate(OBJECTS.get("patternPropsOverrideAdditionalProps"));
  }
}
