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

import static org.everit.json.schema.TestSupport.buildWithLocation;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ArraySchemaTest {

    private static final ResourceLoader loader = ResourceLoader.DEFAULT;

    private static final JSONObject ARRAYS = loader.readObj("arraytestcases.json");

    @Test
    public void additionalItemsSchema() {
        ArraySchema.builder()
                .addItemSchema(BooleanSchema.INSTANCE)
                .schemaOfAdditionalItems(NullSchema.INSTANCE)
                .build().validate(ARRAYS.get("additionalItemsSchema"));
    }

    @Test
    public void additionalItemsSchemaFailure() {
        NullSchema nullSchema = buildWithLocation(NullSchema.builder());
        ArraySchema subject = buildWithLocation(
                ArraySchema.builder()
                        .addItemSchema(buildWithLocation(BooleanSchema.builder()))
                        .schemaOfAdditionalItems(nullSchema)
        );
        TestSupport.failureOf(subject)
                .expectedViolatedSchema(nullSchema)
                .expectedPointer("#/2")
                //                 .expectedKeyword("additionalItems")
                .input(ARRAYS.get("additionalItemsSchemaFailure"))
                .expect();
    }

    @Test
    public void booleanItems() {
        ArraySchema subject = ArraySchema.builder().allItemSchema(BooleanSchema.INSTANCE).build();
        TestSupport.expectFailure(subject, BooleanSchema.INSTANCE, "#/2", ARRAYS.get("boolArrFailure"));
    }

    @Test
    public void doesNotRequireExplicitArray() {
        ArraySchema.builder()
                .requiresArray(false)
                .uniqueItems(true)
                .build().validate(ARRAYS.get("doesNotRequireExplicitArray"));
    }

    @Test
    public void maxItems() {
        ArraySchema subject = buildWithLocation(ArraySchema.builder().maxItems(0));
        TestSupport.failureOf(subject)
                .subject(subject)
                .expectedPointer("#")
                .expectedKeyword("maxItems")
                .expectedMessageFragment("expected maximum item count: 0, found: 1")
                .input(ARRAYS.get("onlyOneItem"))
                .expect();
    }

    @Test
    public void minItems() {
        ArraySchema subject = buildWithLocation(ArraySchema.builder().minItems(2));
        TestSupport.failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("minItems")
                .input(ARRAYS.get("onlyOneItem"))
                .expect();
    }

    @Test
    public void noAdditionalItems() {
        ArraySchema subject = ArraySchema.builder()
                .additionalItems(false)
                .addItemSchema(BooleanSchema.INSTANCE)
                .addItemSchema(NullSchema.INSTANCE)
                .build();
        TestSupport.expectFailure(subject, "#", ARRAYS.get("twoItemTupleWithAdditional"));
    }

    @Test
    public void noItemSchema() {
        ArraySchema.builder().build().validate(ARRAYS.get("noItemSchema"));
    }

    @Test
    public void nonUniqueArrayOfArrays() {
        ArraySchema subject = buildWithLocation(ArraySchema.builder().uniqueItems(true));
        TestSupport.failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("uniqueItems")
                .input(ARRAYS.get("nonUniqueArrayOfArrays"))
                .expect();
    }

    @Test(expected = SchemaException.class)
    public void tupleAndListFailure() {
        ArraySchema.builder().addItemSchema(BooleanSchema.INSTANCE).allItemSchema(NullSchema.INSTANCE)
                .build();
    }

    @Test
    public void tupleWithOneItem() {
        BooleanSchema boolSchema = buildWithLocation(BooleanSchema.builder());
        ArraySchema subject = buildWithLocation(ArraySchema.builder().addItemSchema(boolSchema));
        TestSupport.failureOf(subject)
                .expectedViolatedSchema(boolSchema)
                .expectedPointer("#/0")
                .input(ARRAYS.get("tupleWithOneItem"))
                .expect();
    }

    @Test
    public void subjectHasLessElemsThanTupleEntries() {
        ArraySchema subject = buildWithLocation(ArraySchema.builder()
                .addItemSchema(NullSchema.INSTANCE)
                .addItemSchema(TrueSchema.INSTANCE));
        subject.validate(ARRAYS.get("subjectHasLessElemsThanTupleEntries"));
    }

    @Test
    public void typeFailure() {
        TestSupport.failureOf(ArraySchema.builder())
                .expectedKeyword("type")
                .input(true)
                .expect();
    }

    @Test
    public void uniqueItemsObjectViolation() {
        ArraySchema subject = ArraySchema.builder().uniqueItems(true).build();
        TestSupport.expectFailure(subject, "#", ARRAYS.get("nonUniqueObjects"));
    }

    @Test
    public void uniqueItemsViolation() {
        ArraySchema subject = ArraySchema.builder().uniqueItems(true).build();
        TestSupport.expectFailure(subject, "#", ARRAYS.get("nonUniqueItems"));
    }

    @Test
    public void uniqueItemsWithSameToString() {
        ArraySchema.builder().uniqueItems(true).build()
                .validate(ARRAYS.get("uniqueItemsWithSameToString"));
    }

    @Test
    public void uniqueObjectValues() {
        ArraySchema.builder().uniqueItems(true).build()
                .validate(ARRAYS.get("uniqueObjectValues"));
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(ArraySchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() {
        JSONObject rawSchemaJson = loader.readObj("tostring/arrayschema-list.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void toStringAdditionalItems() {
        JSONObject rawSchemaJson = loader.readObj("tostring/arrayschema-list.json");
        rawSchemaJson.remove("items");
        rawSchemaJson.put("additionalItems", false);
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertFalse(new JSONObject(actual).getBoolean("additionalItems"));
    }

    @Test
    public void toStringNoExplicitType() {
        JSONObject rawSchemaJson = loader.readObj("tostring/arrayschema-list.json");
        rawSchemaJson.remove("type");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void toStringTupleSchema() {
        JSONObject rawSchemaJson = loader.readObj("tostring/arrayschema-tuple.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void toStringContains() {
        JSONObject rawSchemaJson = loader.readObj("tostring/arrayschema-contains.json");
        String actual = SchemaLoader.builder()
                .draftV6Support()
                .schemaJson(rawSchemaJson)
                .build()
                .load()
                .build()
                .toString();
        assertTrue(ObjectComparator.deepEquals(rawSchemaJson, new JSONObject(actual)));
    }

    @Test
    public void containedItemSchemaFailure() {
        TestSupport.failureOf(ArraySchema.builder()
                .containsItemSchema(NullSchema.INSTANCE))
                .expectedKeyword("contains")
                .expectedMessageFragment("expected at least one array item to match 'contains' schema")
                .input(ARRAYS.get("onlyOneItem"))
                .expect();
    }

    @Test
    public void containedItemSchemaEmptyArr() {
        TestSupport.failureOf(ArraySchema.builder()
                .containsItemSchema(NullSchema.INSTANCE))
                .expectedKeyword("contains")
                .expectedMessageFragment("expected at least one array item to match 'contains' schema")
                .input(ARRAYS.get("emptyArray"))
                .expect();
    }

    @Test
    public void requiresArray_nullable() {
        ArraySchema subject = ArraySchema.builder().requiresArray(true).nullable(true).build();
        subject.validate(JSONObject.NULL);
    }

}
