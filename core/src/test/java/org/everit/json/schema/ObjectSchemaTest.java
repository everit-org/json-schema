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
package org.everit.json.schema;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

public class ObjectSchemaTest {

  private static final JSONObject OBJECTS = new JSONObject(new JSONTokener(
      ObjectSchemaTest.class
      .getResourceAsStream("/org/everit/jsonvalidator/objecttestcases.json")));

  @Test
  public void additionalPropertiesOnEmptyObject() {
    ObjectSchema.builder()
    .schemaOfAdditionalProperties(BooleanSchema.INSTANCE).build()
    .validate(OBJECTS.getJSONObject("emptyObject"));
  }

  @Test
  public void additionalPropertySchema() {
    ObjectSchema subject = ObjectSchema.builder()
        .schemaOfAdditionalProperties(BooleanSchema.INSTANCE)
        .build();
    TestSupport.expectFailure(subject, "#/foo", OBJECTS.get("additionalPropertySchema"));
  }

  @Test
  public void maxPropertiesFailure() {
    ObjectSchema subject = ObjectSchema.builder().maxProperties(2).build();
    TestSupport.expectFailure(subject, "#", OBJECTS.get("maxPropertiesFailure"));
  }

  @Test
  public void minPropertiesFailure() {
    ObjectSchema subject = ObjectSchema.builder().minProperties(2).build();
    TestSupport.expectFailure(subject, "#", OBJECTS.get("minPropertiesFailure"));
  }

  @Test
  public void noAdditionalProperties() {
    ObjectSchema subject = ObjectSchema.builder().additionalProperties(false).build();
    TestSupport.expectFailure(subject, "#", OBJECTS.get("propertySchemaViolation"));
  }

  @Test
  public void noProperties() {
    ObjectSchema.builder().build().validate(OBJECTS.get("noProperties"));
  }

  @Test
  public void notRequireObject() {
    ObjectSchema.builder().requiresObject(false).build().validate("foo");
  }

  @Test
  public void patternPropertyOnEmptyObjct() {
    ObjectSchema.builder()
    .patternProperty("b_.*", BooleanSchema.INSTANCE)
    .build().validate(new JSONObject());
  }

  @Test
  public void patternPropertyOverridesAdditionalPropSchema() {
    ObjectSchema.builder()
    .schemaOfAdditionalProperties(new NumberSchema())
    .patternProperty("aa.*", BooleanSchema.INSTANCE)
    .build().validate(OBJECTS.get("patternPropertyOverridesAdditionalPropSchema"));
  }

  @Test
  public void patternPropertyViolation() {
    ObjectSchema subject = ObjectSchema.builder()
        .patternProperty("b_.*", BooleanSchema.INSTANCE)
        .patternProperty("s_.*", new StringSchema())
        .build();
    TestSupport.expectFailure(subject, BooleanSchema.INSTANCE, "#/b_1",
        OBJECTS.get("patternPropertyViolation"));
  }

  @Test
  public void patternPropsOverrideAdditionalProps() {
    ObjectSchema.builder()
    .patternProperty("^v.*", EmptySchema.INSTANCE)
    .additionalProperties(false)
    .build().validate(OBJECTS.get("patternPropsOverrideAdditionalProps"));
  }

  @Test(expected = ValidationException.class)
  public void propertyDepViolation() {
    ObjectSchema.builder()
    .addPropertySchema("ifPresent", NullSchema.INSTANCE)
    .addPropertySchema("mustBePresent", BooleanSchema.INSTANCE)
    .propertyDependency("ifPresent", "mustBePresent")
    .build().validate(OBJECTS.get("propertyDepViolation"));
  }

  @Test(expected = ValidationException.class)
  public void propertySchemaViolation() {
    ObjectSchema.builder().addPropertySchema("boolProp", BooleanSchema.INSTANCE).build()
    .validate(OBJECTS.get("propertySchemaViolation"));
  }

  @Test(expected = ValidationException.class)
  public void requiredProperties() {
    ObjectSchema.builder().addPropertySchema("boolProp", BooleanSchema.INSTANCE)
    .addPropertySchema("nullProp", NullSchema.INSTANCE)
    .addRequiredProperty("boolProp")
    .build().validate(OBJECTS.get("requiredProperties"));
  }

  @Test(expected = ValidationException.class)
  public void schemaDepViolation() {
    ObjectSchema schema = ObjectSchema.builder()
        .addPropertySchema("name", new StringSchema())
        .addPropertySchema("credit_card", NumberSchema.builder().build())
        .schemaDependency("credit_card", ObjectSchema.builder()
            .addPropertySchema("billing_address", new StringSchema())
            .addRequiredProperty("billing_address")
            .build())
            .build();
    schema.validate(OBJECTS.get("schemaDepViolation"));
  }

  @Test(expected = SchemaException.class)
  public void schemaForNoAdditionalProperties() {
    ObjectSchema.builder().additionalProperties(false)
    .schemaOfAdditionalProperties(BooleanSchema.INSTANCE).build();
  }

  @Test(expected = ValidationException.class)
  public void typeFailure() {
    ObjectSchema.builder().build().validate("a");
  }
}
