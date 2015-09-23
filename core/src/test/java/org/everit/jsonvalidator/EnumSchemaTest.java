package org.everit.jsonvalidator;

import java.util.HashSet;
import java.util.Set;

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
    EnumSchema subject = new EnumSchema(possibleValues);
    subject.validate(true);
    subject.validate("foo");
    subject.validate(new JSONArray());
    subject.validate(new JSONObject("{\"a\" : 0}"));
  }

  @Test(expected = ValidationException.class)
  public void failure() {
    EnumSchema subject = new EnumSchema(possibleValues);
    subject.validate(new JSONArray("[1]"));
  }

}
