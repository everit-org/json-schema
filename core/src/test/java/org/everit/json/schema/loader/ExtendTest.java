package org.everit.json.schema.loader;

import java.util.HashMap;

import org.everit.json.schema.ObjectComparator;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;

public class ExtendTest {

  private static JSONObject OBJECTS;

  static {
    OBJECTS = new JSONObject(new JSONTokener(
        ExtendTest.class.getResourceAsStream("/org/everit/jsonvalidator/merge-testcases.json")));
  }

  @Test
  public void additionalHasMoreProps() {
    JSONObject actual = subject().extend(get("propIsTrue"), get("empty"));
    assertEquals(get("propIsTrue"), actual);
  }

  @Test
  public void additionalOverridesOriginal() {
    JSONObject actual = subject().extend(get("propIsTrue"), get("propIsFalse"));
    assertEquals(get("propIsTrue"), actual);
  }

  @Test
  public void additionalPropsAreMerged() {
    JSONObject actual = subject().extend(get("propIsTrue"), get("prop2IsFalse"));
    assertEquals(actual, get("propTrueProp2False"));
  }

  private void assertEquals(final JSONObject expected, final JSONObject actual) {
    Assert.assertTrue(ObjectComparator.deepEquals(expected, actual));
  }

  @Test
  public void bothEmpty() {
    JSONObject actual = subject().extend(get("empty"), get("empty"));
    assertEquals(new JSONObject(), actual);
  }

  private JSONObject get(final String objectName) {
    return OBJECTS.getJSONObject(objectName);
  }

  @Test
  public void multiplePropsAreMerged() {
    JSONObject actual = subject().extend(get("multipleWithPropTrue"), get("multipleWithPropFalse"));
    assertEquals(get("mergedMultiple"), actual);
  }

  @Test
  public void originalPropertyRemainsUnchanged() {
    JSONObject actual = subject().extend(get("empty"), get("propIsTrue"));
    assertEquals(get("propIsTrue"), actual);
  }

  private SchemaLoader subject() {
    return new SchemaLoader("", new JSONObject(), new JSONObject(),
        new HashMap<String, ReferenceSchema.Builder>(), new DefaultSchemaClient());
  }
}
