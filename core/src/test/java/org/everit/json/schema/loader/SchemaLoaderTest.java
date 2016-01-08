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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.EmptySchema;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.NotSchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class SchemaLoaderTest {

  private static JSONObject ALL_SCHEMAS;

  @BeforeClass
  public static void before() {
    InputStream stream = SchemaLoaderTest.class.getResourceAsStream(
        "/org/everit/jsonvalidator/testschemas.json");
    ALL_SCHEMAS = new JSONObject(new JSONTokener(stream));
  }

  private final SchemaClient httpClient = new DefaultSchemaClient();

  @Test
  public void additionalItemSchema() {
    Assert.assertTrue(SchemaLoader.load(get("additionalItemSchema")) instanceof ArraySchema);
  }

  @Test
  public void arrayByAdditionalItems() {
    ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arrayByAdditionalItems"));
    Assert.assertFalse(actual.requiresArray());
  }

  @Test
  public void arrayByItems() {
    ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arrayByItems"));
    Assert.assertNotNull(actual);
  }

  @Test
  public void arraySchema() {
    ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arraySchema"));
    Assert.assertNotNull(actual);
    Assert.assertEquals(2, actual.getMinItems().intValue());
    Assert.assertEquals(3, actual.getMaxItems().intValue());
    Assert.assertTrue(actual.needsUniqueItems());
    Assert.assertEquals(NullSchema.INSTANCE, actual.getAllItemSchema());
  }

  private InputStream asStream(final String string) {
    return new ByteArrayInputStream(string.getBytes());
  }

  @Test
  public void booleanSchema() {
    BooleanSchema actual = (BooleanSchema) SchemaLoader.load(get("booleanSchema"));
    Assert.assertNotNull(actual);
  }

  @Test
  public void combinedSchemaLoading() {
    CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("combinedSchema"));
    Assert.assertNotNull(actual);
  }

  @Test
  public void combinedSchemaWithBaseSchema() {
    CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("combinedSchemaWithBaseSchema"));
    Assert.assertEquals(1, actual.getSubschemas().stream()
        .filter(schema -> schema instanceof StringSchema).count());
    Assert.assertEquals(1, actual.getSubschemas().stream()
        .filter(schema -> schema instanceof CombinedSchema).count());
  }

  @Test
  public void combinedSchemaWithExplicitBaseSchema() {
    CombinedSchema actual = (CombinedSchema) SchemaLoader
        .load(get("combinedSchemaWithExplicitBaseSchema"));
    Assert.assertEquals(1, actual.getSubschemas().stream()
        .filter(schema -> schema instanceof StringSchema).count());
    Assert.assertEquals(1, actual.getSubschemas().stream()
        .filter(schema -> schema instanceof CombinedSchema).count());
  }

  @Test
  public void combinedSchemaWithMultipleBaseSchemas() {
    Schema actual = SchemaLoader.load(get("combinedSchemaWithMultipleBaseSchemas"));
    Assert.assertTrue(actual instanceof CombinedSchema);
  }

  @Test
  public void emptyPatternProperties() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("emptyPatternProperties"));
    Assert.assertNotNull(actual);
    Assert.assertEquals(0, actual.getPatternProperties().size());
  }

  @Test
  public void emptySchema() {
    Assert.assertTrue(SchemaLoader.load(get("emptySchema")) instanceof EmptySchema);
  }

  @Test
  public void emptySchemaWithDefault() {
    EmptySchema actual = (EmptySchema) SchemaLoader.load(get("emptySchemaWithDefault"));
    Assert.assertNotNull(actual);
  }

  @Test
  public void enumSchema() {
    EnumSchema actual = (EnumSchema) SchemaLoader.load(get("enumSchema"));
    Assert.assertNotNull(actual);
    Assert.assertEquals(4, actual.getPossibleValues().size());
  }

  @Test
  public void genericProperties() {
    Schema actual = SchemaLoader.load(get("genericProperties"));
    Assert.assertEquals("myId", actual.getId());
    Assert.assertEquals("my title", actual.getTitle());
    Assert.assertEquals("my description", actual.getDescription());
  }

  private JSONObject get(final String schemaName) {
    return ALL_SCHEMAS.getJSONObject(schemaName);
  }

  @Test
  public void integerSchema() {
    NumberSchema actual = (NumberSchema) SchemaLoader.load(get("integerSchema"));
    Assert.assertEquals(10, actual.getMinimum().intValue());
    Assert.assertEquals(20, actual.getMaximum().intValue());
    Assert.assertEquals(5, actual.getMultipleOf().intValue());
    Assert.assertTrue(actual.isExclusiveMinimum());
    Assert.assertTrue(actual.isExclusiveMaximum());
    Assert.assertTrue(actual.requiresInteger());
  }

  @Test(expected = SchemaException.class)
  public void invalidAdditionalItems() {
    SchemaLoader.load(get("invalidAdditionalItems"));
  }

  @Test(expected = SchemaException.class)
  public void invalidArrayItemSchema() {
    SchemaLoader.load(get("invalidArrayItemSchema"));
  }

  @Test(expected = SchemaException.class)
  public void invalidDependency() {
    SchemaLoader.load(get("invalidDependency"));
  }

  @Test(expected = SchemaException.class)
  public void invalidExclusiveMinimum() {
    SchemaLoader.load(get("invalidExclusiveMinimum"));
  }

  @Test(expected = SchemaException.class)
  public void invalidIntegerSchema() {
    JSONObject input = get("invalidIntegerSchema");
    SchemaLoader.load(input);
  }

  @Test(expected = SchemaException.class)
  public void invalidItemsArraySchema() {
    SchemaLoader.load(get("invalidItemsArraySchema"));
  }

  @Test(expected = SchemaException.class)
  public void invalidStringSchema() {
    SchemaLoader.load(get("invalidStringSchema"));
  }

  @Test(expected = SchemaException.class)
  public void invalidType() {
    SchemaLoader.load(get("invalidType"));
  }

  @Test
  public void jsonPointerInArray() {
    Assert.assertTrue(SchemaLoader.load(get("jsonPointerInArray")) instanceof ArraySchema);
  }

  @Test
  public void multipleTypes() {
    Assert.assertTrue(SchemaLoader.load(get("multipleTypes")) instanceof CombinedSchema);
  }

  @Test
  public void neverMatchingAnyOf() {
    Assert.assertTrue(SchemaLoader.load(get("anyOfNeverMatches")) instanceof CombinedSchema);
  }

  @Test
  public void noExplicitObject() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("noExplicitObject"));
    Assert.assertFalse(actual.requiresObject());
  }

  @Test
  public void notSchema() {
    NotSchema actual = (NotSchema) SchemaLoader.load(get("notSchema"));
    Assert.assertNotNull(actual);
  }

  @Test
  public void nullSchema() {
    NullSchema actual = (NullSchema) SchemaLoader.load(get("nullSchema"));
    Assert.assertNotNull(actual);
  }

  @Test(expected = SchemaException.class)
  public void objectInvalidAdditionalProperties() {
    SchemaLoader.load(get("objectInvalidAdditionalProperties"));
  }

  @Test
  public void objectSchema() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectSchema"));
    Assert.assertNotNull(actual);
    Map<String, Schema> propertySchemas = actual.getPropertySchemas();
    Assert.assertEquals(2, propertySchemas.size());
    Assert.assertEquals(BooleanSchema.INSTANCE, propertySchemas.get("boolProp"));
    Assert.assertFalse(actual.permitsAdditionalProperties());
    Assert.assertEquals(2, actual.getRequiredProperties().size());
    Assert.assertEquals(2, actual.getMinProperties().intValue());
    Assert.assertEquals(3, actual.getMaxProperties().intValue());
  }

  @Test
  public void objectWithAdditionalPropSchema() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithAdditionalPropSchema"));
    Assert.assertEquals(BooleanSchema.INSTANCE, actual.getSchemaOfAdditionalProperties());
  }

  @Test
  public void objectWithPropDep() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithPropDep"));
    Assert.assertEquals(1, actual.getPropertyDependencies().get("isIndividual").size());
  }

  @Test
  public void objectWithSchemaDep() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithSchemaDep"));
    Assert.assertEquals(1, actual.getSchemaDependencies().size());
  }

  @Test
  public void patternProperties() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("patternProperties"));
    Assert.assertNotNull(actual);
    Assert.assertEquals(2, actual.getPatternProperties().size());
  }

  @Test
  public void pointerResolution() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("pointerResolution"));
    ObjectSchema rectangleSchema = (ObjectSchema) ((ReferenceSchema) actual.getPropertySchemas()
        .get("rectangle"))
            .getReferredSchema();
    Assert.assertNotNull(rectangleSchema);
    ReferenceSchema aRef = (ReferenceSchema) rectangleSchema.getPropertySchemas().get("a");
    Assert.assertTrue(aRef.getReferredSchema() instanceof NumberSchema);
  }

  @Test(expected = SchemaException.class)
  public void pointerResolutionFailure() {
    SchemaLoader.load(get("pointerResolutionFailure"));
  }

  @Test
  public void recursiveSchema() {
    SchemaLoader.load(get("recursiveSchema"));
  }

  @Test
  public void remotePointerResulion() {
    SchemaClient httpClient = Mockito.mock(SchemaClient.class);
    Mockito.when(httpClient.get("http://example.org/asd")).thenReturn(asStream("{}"));
    Mockito.when(httpClient.get("http://example.org/otherschema.json")).thenReturn(asStream("{}"));
    SchemaLoader.load(get("remotePointerResolution"), httpClient);
    // Mockito.verify(httpClient);
  }

  @Test
  public void resolutionScopeTest() {
    SchemaLoader.load(get("resolutionScopeTest"), new SchemaClient() {

      @Override
      public InputStream get(final String url) {
        System.out.println("GET " + url);
        return new DefaultSchemaClient().get(url);
      }
    });
  }

  @Test
  public void selfRecursiveSchema() {
    SchemaLoader.load(get("selfRecursiveSchema"));
  }

  @Test
  public void stringSchema() {
    StringSchema actual = (StringSchema) SchemaLoader.load(get("stringSchema"));
    Assert.assertEquals(2, actual.getMinLength().intValue());
    Assert.assertEquals(3, actual.getMaxLength().intValue());
  }

  @Test
  public void tupleSchema() {
    ArraySchema actual = (ArraySchema) SchemaLoader.load(get("tupleSchema"));
    Assert.assertFalse(actual.permitsAdditionalItems());
    Assert.assertNull(actual.getAllItemSchema());
    Assert.assertEquals(2, actual.getItemSchemas().size());
    Assert.assertEquals(BooleanSchema.INSTANCE, actual.getItemSchemas().get(0));
    Assert.assertEquals(NullSchema.INSTANCE, actual.getItemSchemas().get(1));
  }

  @Test(expected = SchemaException.class)
  public void unknownSchema() {
    SchemaLoader.load(get("unknown"));
  }

}
