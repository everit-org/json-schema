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
package org.everit.jsonvalidator.loader;

import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchemaLoaderTest {

  private static JSONObject ALL_SCHEMAS;

  @BeforeClass
  public static void before() {
    InputStream stream = SchemaLoaderTest.class.getResourceAsStream(
        "/org/everit/jsonvalidator/testschemas/hello.json");
    ALL_SCHEMAS = new JSONObject(new JSONTokener(stream));
  }

  @Test
  public void arraySchema() {
    SchemaLoader<JSONArray> actual = SchemaLoader.of(ALL_SCHEMAS.getJSONObject("arraySchema"));
    Assert.assertTrue(actual instanceof ArraySchemaLoader);
  }

  @Test
  public void booleanSchema() {
    SchemaLoader<Boolean> actual = SchemaLoader.of(ALL_SCHEMAS.getJSONObject("booleanSchema"));
    Assert.assertTrue(actual instanceof BooleanSchemaLoader);
  }

  @Test
  public void integerSchema() {
    SchemaLoader<Integer> actual = SchemaLoader.of(ALL_SCHEMAS.getJSONObject("integerSchema"));
  }

  @Test
  public void nullSchema() {
    SchemaLoader<Object> actual = SchemaLoader.of(ALL_SCHEMAS.getJSONObject("nullSchema"));
    Assert.assertTrue(actual instanceof NullSchemaLoader);
  }

  @Test
  public void objectSchema() {
    SchemaLoader<JSONObject> actual = SchemaLoader.of(ALL_SCHEMAS.getJSONObject("objectSchema"));
    Assert.assertTrue(actual instanceof ObjectSchemaLoader);
  }

  @Test
  public void stringSchema() {
    SchemaLoader<String> actual = SchemaLoader.of(ALL_SCHEMAS.getJSONObject("stringSchema"));
    Assert.assertTrue(actual instanceof StringSchemaLoader);
  }

  @Test(expected = IllegalArgumentException.class)
  public void unknownType() {
    SchemaLoader.of(ALL_SCHEMAS.getJSONObject("unknown"));
  }

}
