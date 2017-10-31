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

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.everit.json.schema.TestSupport.buildWithLocation;
import static org.junit.Assert.assertTrue;

public class StringSchemaTest {

    @Test
    public void formatFailure() {
        StringSchema subject = buildWithLocation(StringSchema.builder()
                .formatValidator(subj -> Optional.of("violation")));
        TestSupport.failureOf(subject)
                .expectedKeyword("format")
                .input("string")
                .expect();
    }

    @Test
    public void formatSuccess() {
        StringSchema subject = StringSchema.builder().formatValidator(subj -> Optional.empty()).build();
        subject.validate("string");
    }

    @Test
    public void maxLength() {
        StringSchema subject = buildWithLocation(StringSchema.builder().maxLength(3));
        TestSupport.failureOf(subject)
                .expectedKeyword("maxLength")
                .input("foobar")
                .expect();
    }

    @Test
    public void minLength() {
        StringSchema subject = buildWithLocation(StringSchema.builder().minLength(2));
        TestSupport.failureOf(subject)
                .expectedKeyword("minLength")
                .input("a")
                .expect();
    }

    @Test
    public void multipleViolations() {
        try {
            StringSchema.builder().minLength(3).maxLength(1).pattern("^b.*").build().validate("ab");
            Assert.fail();
        } catch (ValidationException e) {
            Assert.assertEquals(3, e.getCausingExceptions().size());
        }
    }

    @Test
    public void notRequiresString() {
        StringSchema.builder().requiresString(false).build().validate(2);
    }

    @Test
    public void patternFailure() {
        StringSchema subject = buildWithLocation(StringSchema.builder().pattern("^a*$"));
        TestSupport.failureOf(subject).expectedKeyword("pattern").input("abc").expect();
    }

    @Test
    public void patternSuccess() {
        StringSchema.builder().pattern("^a*$").build().validate("aaaa");
    }

    @Test
    public void success() {
        StringSchema.builder().build().validate("foo");
    }

    @Test
    public void typeFailure() {
        TestSupport.failureOf(StringSchema.builder())
                .expectedKeyword("type")
                .input(null)
                .expect();
    }

    @Test(expected = ValidationException.class)
    public void issue38Pattern() {
        StringSchema.builder().requiresString(true).pattern("\\+?\\d+").build().validate("aaa");
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(StringSchema.class)
                .withRedefinedSuperclass()
                .withPrefabValues(JsonObject.class, new JsonObject(Collections.emptyMap()), new JsonObject(null))
                .withIgnoredFields("schemaLocation", "notSchema")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void toStringNoFormat() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.remove("format");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void toStringNoExplicitType() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.remove("type");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void notSchemaEnumValidationSuccess() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("not-enum-schema.json");
        SchemaLoader.load(rawSchemaJson).validate(new JSONObject("{ \"name\": \"ABC\" }"));
    }

    @Test(expected = ValidationException.class)
    public void notSchemaEnumValidationFailTest() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("not-enum-schema.json");
        SchemaLoader.load(rawSchemaJson).validate(new JSONObject("{ \"name\": \"C\" }"));
        //
        Assert.fail("Validation should have failed!");
    }

    @Test
    public void notSchemaPatternValidationSuccess() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("not-hexpattern-schema.json");
        SchemaLoader.load(rawSchemaJson).validate(new JSONObject("{ \"name\": \"deadbeefbadfood!\" }"));
    }

    @Test(expected = ValidationException.class)
    public void notSchemaPatternValidationFailTest() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("not-hexpattern-schema.json");
        SchemaLoader.load(rawSchemaJson).validate(new JSONObject("{ \"name\": \"deadbeefbadf00d1\" }"));
        //
        Assert.fail("Validation should have failed!");
    }

}
