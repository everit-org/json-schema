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

import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.everit.json.schema.TestSupport.buildWithLocation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

public class NotSchemaTest {

    private static final ResourceLoader LOADER = ResourceLoader.DEFAULT;

    @Test
    public void failure() {
        NotSchema subject = buildWithLocation(NotSchema.builder().mustNotMatch(BooleanSchema.INSTANCE));
        TestSupport.failureOf(subject)
                .input(true)
                .expectedKeyword("not")
                .expect();
    }

    @Test
    public void success() {
        NotSchema.builder().mustNotMatch(BooleanSchema.INSTANCE).build().validate("foo");
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(NotSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation", "location")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() {
        NotSchema subject = NotSchema.builder()
                .mustNotMatch(BooleanSchema.INSTANCE)
                .build();
        String actual = subject.toString();
        assertEquals("{\"not\":{\"type\":\"boolean\"}}", actual);
    }

    @Test
    public void issue345() {
        JSONObject rawSchema = LOADER.readObj("issue345.json");
        Schema notSchema = SchemaLoader.builder()
            .schemaJson(rawSchema)
            .build().load().build();
        assertThat(new JSONObject(notSchema.toString()), sameJsonAs(rawSchema));
    }

}
