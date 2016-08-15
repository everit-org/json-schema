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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EnumSchemaTest {

  private Set<Object> possibleValues;

  @Before
  public void before() {
    possibleValues = new HashSet<>();
    possibleValues.add(true);
    possibleValues.add("foo");
  }

  @Test
  public void failure() {
    TestSupport.failureOf(subject())
        .expectedPointer("#")
        .expectedKeyword("enum")
        .input(new JSONArray("[1]"))
        .expect();
  }

  private EnumSchema subject() {
    return EnumSchema.builder().possibleValues(possibleValues).build();
  }

  @Test
  public void success() {
    possibleValues.add(new JSONArray());
    possibleValues.add(new JSONObject("{\"a\" : 0}"));
    EnumSchema subject = subject();
    subject.validate(true);
    subject.validate("foo");
    subject.validate(new JSONArray());
    subject.validate(new JSONObject("{\"a\" : 0}"));
  }

  private Set<Object> asSet(final JSONArray array) {
    return new HashSet<>(IntStream.range(0, array.length())
        .mapToObj(i -> array.get(i))
        .collect(Collectors.toSet()));
  }

  @Test
  public void toStringTest() {
    StringWriter buffer = new StringWriter();
    subject().describeTo(new JSONWriter(buffer));
    JSONObject actual = new JSONObject(buffer.getBuffer().toString());
    Assert.assertEquals(2, JSONObject.getNames(actual).length);
    Assert.assertEquals("enum", actual.get("type"));
    JSONArray pv =
        new JSONArray(Arrays.asList(true, "foo"));
    Assert.assertEquals(asSet(pv), asSet(actual.getJSONArray("enum")));
  }
}
