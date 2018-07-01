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
import org.everit.json.schema.loader.JsonValue;
import org.junit.Assert;
import org.junit.Test;

public class EmptySchemaTest {

    @Test
    public void testValidate() {
        EmptySchema.INSTANCE.validate("something");
    }

    @Test
    public void testBuilder() {
        Assert.assertEquals(EmptySchema.builder().build(), EmptySchema.builder().build());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("{}", EmptySchema.INSTANCE.toString());
    }

    private JsonObject json(final String title, final String description, final String id) {
        return (JsonObject)JsonValue.of(JsonSchemaUtil.stringToNode(EmptySchema.builder().title(title).description(description).id(id)
                .build().toString()));
    }

    @Test
    public void testOnlySchemaDescription() {
    	JsonObject actual = json(null, "descr", null);
        Assert.assertEquals(1, actual.getNames().length);
        Assert.assertEquals("descr", actual.get("description"));
    }

    @Test
    public void testOnlyTitle() {
    	JsonObject actual = json("my title", null, null);
        Assert.assertEquals(1, actual.getNames().length);
        Assert.assertEquals("my title", actual.get("title"));
    }

    @Test
    public void testOnlyId() {
    	JsonObject actual = json(null, null, "my/id");
        Assert.assertEquals(1, actual.getNames().length);
        Assert.assertEquals("my/id", actual.get("id"));
    }

    @Test
    public void testAllGenericProps() {
    	JsonObject actual = json("my title", "my description", "my/id");
        Assert.assertEquals(3, actual.getNames().length);
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(EmptySchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
