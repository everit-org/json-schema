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

import static java.util.Collections.emptyMap;
import static org.everit.json.schema.TestSupport.buildWithLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.everit.json.schema.ReferenceSchema.Builder;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ReferenceSchemaTest {

    public static final ImmutableMap<String, Object> UNPROC_PROPS = ImmutableMap.of("unproc", true);

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
    public void validationShouldDelegateToReferredSchema() {
        ReferenceSchema subject = ReferenceSchema.builder().build();
        BooleanSchema referredSchema = buildWithLocation(BooleanSchema.builder());
        subject.setReferredSchema(referredSchema);
        TestSupport.failureOf(subject)
                .input("asd")
                .expectedViolatedSchema(referredSchema)
                .expectedKeyword("type")
                .expect();
    }

    @Test(expected = IllegalStateException.class)
    public void validateThrowsExc_IfNoReferredSchemaIsSet() {
        ReferenceSchema subject = ReferenceSchema.builder().build();
        subject.validate(null);
    }

    @Test(expected = IllegalStateException.class)
    public void definesPropertyThrowsExc_IfNoReferredSchemaIsSet() {
        ReferenceSchema subject = ReferenceSchema.builder().build();
        subject.definesProperty("propName");
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(ReferenceSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation", "location")
                //there are specifically some non final fields for loading of recursive schemas
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.STRICT_INHERITANCE)
                .suppress(Warning.STRICT_HASHCODE)
                .verify();
    }

    @Test
    public void unprocessedPropertiesBeforeSettingRefSchema() {
        ReferenceSchema subject = ReferenceSchema.builder()
                .unprocessedProperties(UNPROC_PROPS)
                .refValue("#/pointer")
                .build();
        subject.setReferredSchema(EmptySchema.builder().build());
        assertEquals(UNPROC_PROPS, subject.getUnprocessedProperties());
    }

    @Test
    public void unprocessedPropertiesAfterSettingRefSchema() {
        ReferenceSchema subject = ReferenceSchema.builder()
                .refValue("#/pointer")
                .unprocessedProperties(UNPROC_PROPS)
                .build();
        subject.setReferredSchema(EmptySchema.builder().build());
        assertEquals(UNPROC_PROPS, subject.getUnprocessedProperties());
    }

    @Test
    public void unprocessedPropertiesAfterBuild() {
        Builder builder = ReferenceSchema.builder()
                .refValue("#/pointer");
        ReferenceSchema subject = builder
                .build();
        subject.setReferredSchema(EmptySchema.builder().build());
        builder.unprocessedProperties(UNPROC_PROPS);
        assertEquals(UNPROC_PROPS, subject.getUnprocessedProperties());
    }

    @Test
    public void emptyUnprocessedProperties() {
        ReferenceSchema subject = ReferenceSchema.builder()
                .refValue("#/pointer")
                .build();
        subject.setReferredSchema(EmptySchema.builder().build());
        assertEquals(emptyMap(), subject.getUnprocessedProperties());
    }

    @Test
    public void toStringTest() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/ref.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson.query("/properties"),
                new JSONObject(actual).query("/properties")));
    }

}
