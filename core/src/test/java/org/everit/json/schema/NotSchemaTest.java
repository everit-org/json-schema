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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NotSchemaTest {

    @Test
    public void failure() {
        NotSchema subject = NotSchema.builder().mustNotMatch(BooleanSchema.INSTANCE).build();
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

}
