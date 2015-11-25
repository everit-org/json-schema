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

public class ArraySchemaTest {

  private static final JSONObject ARRAYS = new JSONObject(new JSONTokener(
      ArraySchemaTest.class.getResourceAsStream("/org/everit/jsonvalidator/arraytestcases.json")));

  @Test
  public void additionalItemsSchema() {
    ArraySchema.builder()
        .addItemSchema(BooleanSchema.INSTANCE)
        .schemaOfAdditionalItems(NullSchema.INSTANCE)
        .build().validate(ARRAYS.get("additionalItemsSchema"));
  }

  @Test(expected = ValidationException.class)
  public void additionalItemsSchemaFailure() {
    ArraySchema.builder()
        .addItemSchema(BooleanSchema.INSTANCE)
        .schemaOfAdditionalItems(NullSchema.INSTANCE)
        .build().validate(ARRAYS.get("additionalItemsSchemaFailure"));
  }

  @Test(expected = ValidationException.class)
  public void booleanItems() {
    ArraySchema.builder().allItemSchema(BooleanSchema.INSTANCE).build()
        .validate(ARRAYS.get("boolArrFailure"));
  }

  @Test
  public void doesNotRequireExplicitArray() {
    ArraySchema.builder()
        .requiresArray(false)
        .uniqueItems(true)
        .build().validate(ARRAYS.get("doesNotRequireExplicitArray"));
  }

  private void exceptFailure(final Schema failingSchema, final String testInputName) {
    TestSupport.expectFailure(failingSchema, ARRAYS.get(testInputName));
  }

  @Test
  public void maxItems() {
    ArraySchema subject = ArraySchema.builder().maxItems(0).build();
    exceptFailure(subject, "onlyOneItem");
  }

  @Test
  public void minItems() {
    ArraySchema subject = ArraySchema.builder().minItems(2).build();
    exceptFailure(subject, "onlyOneItem");
  }

  @Test
  public void noAdditionalItems() {
    ArraySchema subject = ArraySchema.builder()
        .additionalItems(false)
        .addItemSchema(BooleanSchema.INSTANCE)
        .addItemSchema(NullSchema.INSTANCE)
        .build();
    exceptFailure(subject, "twoItemTupleWithAdditional");
  }

  @Test
  public void noItemSchema() {
    ArraySchema.builder().build().validate(ARRAYS.get("noItemSchema"));
  }

  @Test(expected = ValidationException.class)
  public void nonUniqueArrayOfArrays() {
    ArraySchema.builder().uniqueItems(true).build().validate(ARRAYS.get("nonUniqueArrayOfArrays"));
  }

  @Test(expected = SchemaException.class)
  public void tupleAndListFailure() {
    ArraySchema.builder().addItemSchema(BooleanSchema.INSTANCE).allItemSchema(NullSchema.INSTANCE)
        .build();
  }

  @Test(expected = ValidationException.class)
  public void tupleWithOneItem() {
    ArraySchema.builder().addItemSchema(BooleanSchema.INSTANCE).build()
        .validate(ARRAYS.get("tupleWithOneItem"));
  }

  @Test(expected = ValidationException.class)
  public void typeFailure() {
    ArraySchema.builder().build().validate(true);
  }

  @Test(expected = ValidationException.class)
  public void uniqueItemsObjectViolation() {
    ArraySchema.builder().uniqueItems(true).build().validate(ARRAYS.get("nonUniqueObjects"));
  }

  @Test(expected = ValidationException.class)
  public void uniqueItemsViolation() {
    ArraySchema.builder().uniqueItems(true).build().validate(ARRAYS.get("nonUniqueItems"));
  }

  @Test
  public void uniqueItemsWithSameToString() {
    ArraySchema.builder().uniqueItems(true).build()
        .validate(ARRAYS.get("uniqueItemsWithSameToString"));
  }

  @Test
  public void uniqueObjectValues() {
    ArraySchema.builder().uniqueItems(true).build()
        .validate(ARRAYS.get("uniqueObjectValues"));
  }
}
