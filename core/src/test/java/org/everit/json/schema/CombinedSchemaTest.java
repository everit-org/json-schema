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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.junit.Test;

public class CombinedSchemaTest {

  @Test
  public void factories() {
    CombinedSchema.allOf((Collection)Arrays.asList(BooleanSchema.INSTANCE));
    CombinedSchema.anyOf((Collection)Arrays.asList(BooleanSchema.INSTANCE));
    CombinedSchema.oneOf((Collection)Arrays.asList(BooleanSchema.INSTANCE));
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

  private static final List<Schema> SUBSCHEMAS = (List)
		  Arrays.asList(
      NumberSchema.builder().multipleOf(10).build(),
      NumberSchema.builder().multipleOf(3).build()
      );

  @Test(expected = ValidationException.class)
  public void validateAll() {
    CombinedSchema.allOf(SUBSCHEMAS).build()
    .validate(20);
  }

  @Test(expected = ValidationException.class)
  public void validateAny() {
    CombinedSchema.anyOf(SUBSCHEMAS).build()
    .validate(5);
  }

  @Test(expected = ValidationException.class)
  public void validateOne() {
    CombinedSchema.oneOf(SUBSCHEMAS).build()
    .validate(30);
  }

  @Test(expected = ValidationException.class)
  public void anyOfInvalid() 
  {
    CombinedSchema.anyOf((Collection)Arrays.asList(
        StringSchema.builder().maxLength(2).build(),
        StringSchema.builder().minLength(4).build()))
        .build().validate("foo");
  }

}
