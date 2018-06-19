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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.everit.json.schema.internal.JsonPrinter;
import org.everit.json.schema.loader.JsonArray;
import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.JsonValue;
import org.junit.Before;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

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
                .input(JsonValue.of(JsonSchemaUtil.stringToNode("[1]")))
                .expect();
    }

    private EnumSchema.Builder subject() {
        return EnumSchema.builder().possibleValues(possibleValues);
    }

    @Test
    public void success() {
        possibleValues.add(new JsonArray(new ArrayList()));
        possibleValues.add(JsonValue.of(JsonSchemaUtil.stringToNode("{\"a\" : 0}")));
        EnumSchema subject = subject().build();
        subject.validate(true);
        subject.validate("foo");
        subject.validate(new JsonArray(new ArrayList()));
        subject.validate(JsonValue.of(JsonSchemaUtil.stringToNode("{\"a\" : 0}")));
    }

    @Test
    public void objectInArrayMatches() {
        possibleValues.add(JsonValue.of(JsonSchemaUtil.stringToNode("[{\"a\" : true}]")));
        EnumSchema subject = subject().build();
        subject.validate(JsonValue.of(JsonSchemaUtil.stringToNode("[{\"a\" : true}]")));
    }

    private Set<Object> asSet(final JsonArray array) {
        return new HashSet<>(IntStream.range(0, array.length())
                .mapToObj(i -> array.get(i))
                .collect(Collectors.toSet()));
    }

    @Test
    public void toStringTest() {
        StringWriter buffer = new StringWriter();
        subject().build().describeTo(new JsonPrinter(buffer));
        JsonObject actual = (JsonObject)JsonValue.of(JsonSchemaUtil.stringToNode(buffer.getBuffer().toString()));
        assertEquals(1, actual.getNames().length);
        JsonArray pv = (JsonArray)JsonValue.of(JsonSchemaUtil.stringToNode("[true, \"foo\"]"));
        assertEquals(asSet(pv), asSet((JsonArray)actual.get("enum")));
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(EnumSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void nullSuccess() {
        EnumSchema.builder().possibleValue(JsonValue.of(JsonSchemaUtil.stringToNode("null"))).build().validate(JsonObject.NULL);
    }

}
