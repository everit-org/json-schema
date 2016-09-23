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
import org.everit.json.schema.ReferenceSchema.Builder;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ReferenceSchemaTest {

    @Test
    public void constructorMustRunOnlyOnce() {
        Builder builder = ReferenceSchema.builder();
        Assert.assertSame(builder.build(), builder.build());
    }

    @Test(expected = IllegalStateException.class)
    public void setterShouldWorkOnlyOnce() {
        ReferenceSchema subject = ReferenceSchema.builder().build();
        subject.setReferredSchema(BooleanSchema.INSTANCE);
        subject.setReferredSchema(BooleanSchema.INSTANCE);
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(ReferenceSchema.class)
                .withRedefinedSuperclass()
                //there are specifically some non final fields for loading of recursive schemas
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/ref.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        System.out.println(actual);
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson.query("/properties"),
                new JSONObject(actual).query("/properties")));
    }

}
