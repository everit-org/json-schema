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

  @Test
  public void additionalItemsSchemaFailure() {
    ArraySchema subject = ArraySchema.builder()
        .addItemSchema(BooleanSchema.INSTANCE)
        .schemaOfAdditionalItems(NullSchema.INSTANCE)
        .build();
    TestSupport.failure()
        .subject(subject)
        .expectedViolatedSchema(NullSchema.INSTANCE)
        .expectedPointer("#/2")
        // .expectedKeyword("additionalItems")
        .input(ARRAYS.get("additionalItemsSchemaFailure"))
        .expect();
  }

  @Test
  public void booleanItems() {
    ArraySchema subject = ArraySchema.builder().allItemSchema(BooleanSchema.INSTANCE).build();
    TestSupport.expectFailure(subject, BooleanSchema.INSTANCE, "#/2", ARRAYS.get("boolArrFailure"));
  }

  @Test
  public void doesNotRequireExplicitArray() {
    ArraySchema.builder()
        .requiresArray(false)
        .uniqueItems(true)
        .build().validate(ARRAYS.get("doesNotRequireExplicitArray"));
  }

  @Test
  public void maxItems() {
    ArraySchema subject = ArraySchema.builder().maxItems(0).build();
    TestSupport.failure()
        .subject(subject)
        .expectedPointer("#")
        .expectedKeyword("maxItems")
        .input(ARRAYS.get("onlyOneItem"))
        .expect();
  }

  @Test
  public void minItems() {
    ArraySchema subject = ArraySchema.builder().minItems(2).build();
    TestSupport.failure()
        .subject(subject)
        .expectedPointer("#")
        .expectedKeyword("minItems")
        .input(ARRAYS.get("onlyOneItem"))
        .expect();
  }

  @Test
  public void noAdditionalItems() {
    ArraySchema subject = ArraySchema.builder()
        .additionalItems(false)
        .addItemSchema(BooleanSchema.INSTANCE)
        .addItemSchema(NullSchema.INSTANCE)
        .build();
    TestSupport.expectFailure(subject, "#", ARRAYS.get("twoItemTupleWithAdditional"));
  }

  @Test
  public void noItemSchema() {
    ArraySchema.builder().build().validate(ARRAYS.get("noItemSchema"));
  }

  @Test
  public void nonUniqueArrayOfArrays() {
    ArraySchema subject = ArraySchema.builder().uniqueItems(true).build();
    TestSupport.failure()
        .subject(subject)
        .expectedPointer("#")
        .expectedKeyword("uniqueItems")
        .input(ARRAYS.get("nonUniqueArrayOfArrays"))
        .expect();
  }

  @Test(expected = SchemaException.class)
  public void tupleAndListFailure() {
    ArraySchema.builder().addItemSchema(BooleanSchema.INSTANCE).allItemSchema(NullSchema.INSTANCE)
        .build();
  }

  @Test
  public void tupleWithOneItem() {
    ArraySchema subject = ArraySchema.builder().addItemSchema(BooleanSchema.INSTANCE).build();
    TestSupport.expectFailure(subject, BooleanSchema.INSTANCE, "#/0",
        ARRAYS.get("tupleWithOneItem"));
  }

  @Test
  public void typeFailure() {
    TestSupport.expectFailure(new TestSupport.Failure()
        .subject(ArraySchema.builder().build())
        .expectedKeyword("type")
        .input(true));
  }

  @Test
  public void uniqueItemsObjectViolation() {
    ArraySchema subject = ArraySchema.builder().uniqueItems(true).build();
    TestSupport.expectFailure(subject, "#", ARRAYS.get("nonUniqueObjects"));
  }

  @Test
  public void uniqueItemsViolation() {
    ArraySchema subject = ArraySchema.builder().uniqueItems(true).build();
    TestSupport.expectFailure(subject, "#", ARRAYS.get("nonUniqueItems"));
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
