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

import java.util.HashSet;
import java.util.Set;

import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class EnumSchemaTest {

  private Set<Object> possibleValues;

  @Before
  public void before() {
    possibleValues = new HashSet<>();
    possibleValues.add(true);
    possibleValues.add("foo");
    possibleValues.add(new JSONArray());
    possibleValues.add(new JSONObject("{\"a\" : 0}"));
  }

  @Test
  public void success() {
    EnumSchema subject = subject();
    subject.validate(true);
    subject.validate("foo");
    subject.validate(new JSONArray());
    subject.validate(new JSONObject("{\"a\" : 0}"));
  }

  private EnumSchema subject() {
    return EnumSchema.builder().possibleValues(possibleValues).build();
  }

  @Test(expected = ValidationException.class)
  public void failure() {
    EnumSchema subject = subject();
    subject.validate(new JSONArray("[1]"));
  }

}
