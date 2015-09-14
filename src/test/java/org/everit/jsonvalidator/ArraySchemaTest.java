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

public class ArraySchemaTest {

  private static final JSONObject ARRAYS = new JSONObject(new JSONTokener(
      ArraySchemaTest.class.getResourceAsStream("/org/everit/jsonvalidator/arraytestcases.json")));

  @Test(expected = ValidationException.class)
  public void maxItems() {
    ArraySchema.builder().maxItems(0).build().validate(ARRAYS.get("onlyOneItem"));
  }

  @Test(expected = ValidationException.class)
  public void minItems() {
    ArraySchema.builder().minItems(2).build().validate(ARRAYS.get("onlyOneItem"));
  }

  @Test
  public void noItemSchema() {
    ArraySchema.builder().build().validate(ARRAYS.get("noItemSchema"));
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

}
