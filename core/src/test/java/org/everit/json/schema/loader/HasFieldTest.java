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
package org.everit.json.schema.loader;

import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;

public class HasFieldTest {

  private static JSONObject ALL_SCHEMAS;

  @BeforeClass
  public static void before() {
    InputStream stream = HasFieldTest.class.getResourceAsStream(
            "/org/everit/jsonvalidator/testschemas.json");
    ALL_SCHEMAS = new JSONObject(new JSONTokener(stream));
  }

  private JSONObject get(final String schemaName) {
    return ALL_SCHEMAS.getJSONObject(schemaName);
  }

  @Test
  public void objectSchemaHasField() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("pointerResolution"));
    Assert.assertTrue(actual.hasField("rectangle"));
    Assert.assertTrue(actual.hasField("rectangle.a"));
    Assert.assertTrue(actual.hasField("rectangle.b"));

    Assert.assertFalse(actual.hasField("rectangle.c"));
    Assert.assertFalse(actual.hasField("rectangle."));
    Assert.assertFalse(actual.hasField("."));
    Assert.assertFalse(actual.hasField(".a"));
    Assert.assertFalse(actual.hasField(""));
    Assert.assertFalse(actual.hasField("rectangle.a.d"));
  }

  @Test
  public void recursiveSchemaHasField() {
    Schema recursiveSchema = SchemaLoader.load(get("recursiveSchema"));

    Assert.assertTrue(recursiveSchema.hasField("prop"));
    Assert.assertTrue(recursiveSchema.hasField("prop.subprop"));
    Assert.assertTrue(recursiveSchema.hasField("prop.subprop.subprop"));
    Assert.assertTrue(recursiveSchema.hasField("prop.subprop.subprop.subprop"));
  }

  @Test
  public void patternPropertiesHasField() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("patternProperties"));
    Assert.assertTrue(actual.hasField("a"));
    Assert.assertTrue(actual.hasField("aa"));
    Assert.assertTrue(actual.hasField("aaa"));
    Assert.assertTrue(actual.hasField("aaaa"));
    Assert.assertTrue(actual.hasField("aaaaa"));

    Assert.assertFalse(actual.hasField("b"));
  }

}
