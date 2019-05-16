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

import org.everit.json.schema.ContextualFormatValidator;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class CustomFormatValidatorExtTest {

    private final ResourceLoader loader = ResourceLoader.DEFAULT;

    static class DivisibleValidator implements ContextualFormatValidator {

        @Override
        public Optional<String> validate(final String subject, final Map<String, Object> unprocessedProperties) {
            if (unprocessedProperties.containsKey("factors")) {
                try {
                    long divisible = Long.parseLong(subject);
                    try {
                        List<String> factors = (List<String>) unprocessedProperties.get("factors");
                        for (String strFactor: factors) {
                            try {
                                long factor = Long.parseLong(strFactor);
                                if (divisible % factor != 0) {
                                    return Optional.of(String.format("[%s] is not divisible by [%s]", subject, strFactor));
                                }
                            } catch(NumberFormatException nfe) {
                                return Optional.of(String.format("the factor [%s] is not a parsable number", strFactor));
                            }
                        }
                        return Optional.empty();
                    } catch(ClassCastException cce) {
                        return Optional.of("'factors' is not an array in JSON Schema");
                    }
                } catch(NumberFormatException nfe) {
                    return Optional.of(String.format("the string [%s] is not a parsable number", subject));
                }
            } else {
                return Optional.of(String.format("no declared 'factors' in JSON Schema", subject));
            }
        }

        @Override
        public String formatName() {
            return "divisible";
        }
    }

    @Test
    public void testWorks() {
        SchemaLoader schemaLoader = SchemaLoader.builder()
                .schemaJson(baseSchemaJson())
                .addFormatValidator("divisible", new DivisibleValidator())
                .build();
        
        try {
            schemaLoader.load().build().validate(loader.readObj("customformat-ext-works_data.json"));
        } catch (ValidationException ve) {
            Assert.fail("throwed exception");
        }
    }

    @Test
    public void testFail() {
        SchemaLoader schemaLoader = SchemaLoader.builder()
                .schemaJson(baseSchemaJson())
                .addFormatValidator("divisible", new DivisibleValidator())
                .build();
        
        try {
            schemaLoader.load().build().validate(loader.readObj("customformat-ext-fail_data.json"));
            Assert.fail("did not throw exception");
        } catch (ValidationException ve) {
        }
    }

    @Test
    public void nameOverride() {
        JSONObject rawSchemaJson = baseSchemaJson();
        JSONObject idPropSchema = (JSONObject) rawSchemaJson.query("/properties/v");
        idPropSchema.put("format", "somethingelse");
        SchemaLoader schemaLoader = SchemaLoader.builder()
                .schemaJson(rawSchemaJson)
                .addFormatValidator("somethingelse", new DivisibleValidator())
                .build();
        Object actual = fetchFormatValueFromOutputJson(schemaLoader);
        assertEquals("somethingelse", actual);
    }

    private Object fetchFormatValueFromOutputJson(SchemaLoader schemaLoader) {
        return new JSONObject(schemaLoader.load().build().toString())
                .query("/properties/v/format");
    }

    private JSONObject baseSchemaJson() {
        return loader.readObj("customformat-ext-schema.json");
    }

}
