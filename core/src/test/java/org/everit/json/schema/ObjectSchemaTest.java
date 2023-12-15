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
import static java.util.stream.Collectors.toMap;
import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.everit.json.schema.TestSupport.buildWithLocation;
import static org.everit.json.schema.TestSupport.loadAsV6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.junit.jupiter.api.Test;

import com.google.re2j.Pattern;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ObjectSchemaTest {

    private static final Map<String, Schema> toStringToSchemaMap(Map<java.util.regex.Pattern, Schema> original) {
        return original.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey().toString(), entry.getValue()))
                .collect(toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue()
                ));
    }

    private static final JSONObject OBJECTS = ResourceLoader.DEFAULT.readObj("objecttestcases.json");

    private final ResourceLoader loader = ResourceLoader.DEFAULT;

    public static final Schema MULTIPLE_VIOLATIONS_SCHEMA = ObjectSchema.builder()
            .addPropertySchema("numberProp", new NumberSchema())
            .patternProperty("^string.*", new StringSchema())
            .addPropertySchema("boolProp", BooleanSchema.INSTANCE)
            .addRequiredProperty("boolProp")
            .build();

    @Test
    public void additionalPropertiesOnEmptyObject() {
        ObjectSchema.builder()
                .schemaOfAdditionalProperties(BooleanSchema.INSTANCE).build()
                .validate(OBJECTS.getJSONObject("emptyObject"));
    }

    @Test
    public void additionalPropertySchema() {
        String expectedSchemaLocation = "#/bool/location";
        BooleanSchema boolSchema = BooleanSchema.builder().schemaLocation(expectedSchemaLocation).build();
        ObjectSchema subject = buildWithLocation(ObjectSchema.builder()
                .schemaOfAdditionalProperties(boolSchema));
        TestSupport.failureOf(subject)
                .input(OBJECTS.get("additionalPropertySchema"))
                .expectedPointer("#/foo")
                .expectedSchemaLocation(expectedSchemaLocation)
                .expect();
        TestSupport.expectFailure(subject, "#/foo", OBJECTS.get("additionalPropertySchema"));
    }

    @Test
    public void maxPropertiesFailure() {
        ObjectSchema subject = buildWithLocation(ObjectSchema.builder().maxProperties(2));
        TestSupport.failureOf(subject)
                .input(OBJECTS.get("maxPropertiesFailure"))
                .expectedPointer("#")
                .expectedKeyword("maxProperties")
                .expect();
    }

    @Test
    public void minPropertiesFailure() {
        ObjectSchema subject = buildWithLocation(ObjectSchema.builder().minProperties(2));
        TestSupport.failureOf(subject)
                .input(OBJECTS.get("minPropertiesFailure"))
                .expectedPointer("#")
                .expectedKeyword("minProperties")
                .expect();
    }

    @Test
    public void multipleAdditionalProperties() {
        ObjectSchema subject = buildWithLocation(ObjectSchema.builder().additionalProperties(false));
        try {
            subject.validate(new JSONObject("{\"a\":true,\"b\":true}"));
            fail("did not throw exception for multiple additional properties");
        } catch (ValidationException e) {
            assertEquals("#: 2 schema violations found", e.getMessage());
            assertEquals(2, e.getCausingExceptions().size());
        }
    }

    @Test
    public void multipleSchemaDepViolation() {
        Schema billingAddressSchema = new StringSchema();
        Schema billingNameSchema = StringSchema.builder().minLength(4).build();
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("name", new StringSchema())
                .addPropertySchema("credit_card", NumberSchema.builder().build())
                .schemaDependency("credit_card", ObjectSchema.builder()
                        .addPropertySchema("billing_address", billingAddressSchema)
                        .addRequiredProperty("billing_address")
                        .addPropertySchema("billing_name", billingNameSchema)
                        .build())
                .schemaDependency("name", ObjectSchema.builder()
                        .addRequiredProperty("age")
                        .build())
                .build();
        try {
            subject.validate(OBJECTS.get("schemaDepViolation"));
            fail("did not throw ValidationException");
        } catch (ValidationException e) {
            ValidationException creditCardFailure = e.getCausingExceptions().get(0);
            ValidationException ageFailure = e.getCausingExceptions().get(1);
            // due to schemaDeps being stored in (unsorted) HashMap, the exceptions may need to be swapped
            if (creditCardFailure.getCausingExceptions().isEmpty()) {
                ValidationException tmp = creditCardFailure;
                creditCardFailure = ageFailure;
                ageFailure = tmp;
            }
            ValidationException billingAddressFailure = creditCardFailure.getCausingExceptions().get(0);
            ValidationException billingNameFailure = creditCardFailure.getCausingExceptions().get(1);
            if(billingAddressFailure.getPointerToViolation().equals("#/billing_name")) {
                ValidationException tmp = billingAddressFailure;
                billingAddressFailure = billingNameFailure;
                billingNameFailure = tmp;
            }
            assertEquals("#/billing_address", billingAddressFailure.getPointerToViolation());
            assertEquals(billingAddressSchema, billingAddressFailure.getViolatedSchema());
            assertEquals("#/billing_name", billingNameFailure.getPointerToViolation());
            assertEquals(billingNameSchema, billingNameFailure.getViolatedSchema());
            assertEquals("#", ageFailure.getPointerToViolation());
            assertEquals("#: required key [age] not found", ageFailure.getMessage());
        }
    }

    @Test
    public void multipleViolations() {
        try {
            MULTIPLE_VIOLATIONS_SCHEMA.validate(OBJECTS.get("multipleViolations"));
            fail("did not throw exception for 3 schema violations");
        } catch (ValidationException e) {
            assertEquals(3, e.getCausingExceptions().size());
            assertEquals(1, TestSupport.countCauseByJsonPointer(e, "#"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(e, "#/numberProp"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(e, "#/stringPatternMatch"));

            List<String> messages = e.getAllMessages();
            assertEquals(3, messages.size());
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/numberProp:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/stringPatternMatch:"));
        }
    }

    @Test
    public void multipleViolationsNested() throws Exception {
        Callable<ObjectSchema.Builder> newBuilder = () -> ObjectSchema.builder()
                .addPropertySchema("numberProp", new NumberSchema())
                .patternProperty("^string.*", new StringSchema())
                .addPropertySchema("boolProp", BooleanSchema.INSTANCE)
                .addRequiredProperty("boolProp");

        Schema nested2 = newBuilder.call().build();
        Schema nested1 = newBuilder.call().addPropertySchema("nested", nested2).build();
        Schema subject = newBuilder.call().addPropertySchema("nested", nested1).build();

        try {
            subject.validate(OBJECTS.get("multipleViolationsNested"));
            fail("did not throw exception for 9 schema violations");
        } catch (ValidationException subjectException) {
            assertEquals("#: 9 schema violations found", subjectException.getMessage());
            assertEquals(4, subjectException.getCausingExceptions().size());
            assertEquals(1, TestSupport.countCauseByJsonPointer(subjectException, "#"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(subjectException, "#/numberProp"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(subjectException, "#/stringPatternMatch"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(subjectException, "#/nested"));

            ValidationException nested1Exception = subjectException.getCausingExceptions().stream()
                    .filter(ex -> ex.getPointerToViolation().equals("#/nested"))
                    .findFirst()
                    .get();
            assertEquals("#/nested: 6 schema violations found", nested1Exception.getMessage());
            assertEquals(4, nested1Exception.getCausingExceptions().size());
            assertEquals(1, TestSupport.countCauseByJsonPointer(nested1Exception, "#/nested"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(nested1Exception, "#/nested/numberProp"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(nested1Exception, "#/nested/stringPatternMatch"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(nested1Exception, "#/nested/nested"));

            ValidationException nested2Exception = nested1Exception.getCausingExceptions().stream()
                    .filter(ex -> ex.getPointerToViolation().equals("#/nested/nested"))
                    .findFirst()
                    .get();
            assertEquals("#/nested/nested: 3 schema violations found", nested2Exception.getMessage());
            assertEquals(3, nested2Exception.getCausingExceptions().size());
            assertEquals(1, TestSupport.countCauseByJsonPointer(nested2Exception, "#/nested/nested"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(nested2Exception, "#/nested/nested/numberProp"));
            assertEquals(1, TestSupport.countCauseByJsonPointer(nested2Exception, "#/nested/nested/stringPatternMatch"));

            List<String> messages = subjectException.getAllMessages();
            assertEquals(9, messages.size());
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/numberProp:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/stringPatternMatch:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/numberProp:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/stringPatternMatch:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/nested:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/nested/numberProp:"));
            assertEquals(1, TestSupport.countMatchingMessage(messages, "#/nested/nested/stringPatternMatch:"));
        }
    }

    @Test
    public void noAdditionalProperties() {
        ObjectSchema subject = ObjectSchema.builder().additionalProperties(false).build();
        TestSupport.expectFailure(subject, "#", OBJECTS.get("propertySchemaViolation"));
    }

    @Test
    public void noProperties() {
        ObjectSchema.builder().build().validate(OBJECTS.get("noProperties"));
    }

    @Test
    public void notRequireObject() {
        ObjectSchema.builder().requiresObject(false).build().validate("foo");
    }

    @Test
    public void patternPropertyOnEmptyObjct() {
        ObjectSchema.builder()
                .patternProperty("b_.*", BooleanSchema.INSTANCE)
                .build().validate(new JSONObject());
    }

    @Test
    public void patternPropertyTranslation() {
        ObjectSchema subject = ObjectSchema.builder()
                .patternProperty(java.util.regex.Pattern.compile("b_.*"), BooleanSchema.INSTANCE)
                .build();
        Map<java.util.regex.Pattern, Schema> expected = new HashMap<>();
        expected.put(java.util.regex.Pattern.compile("b_.*"), BooleanSchema.INSTANCE);
        assertEquals(toStringToSchemaMap(expected), toStringToSchemaMap(subject.getPatternProperties()));
    }

    @Test
    public void patternPropertyOverridesAdditionalPropSchema() {
        ObjectSchema.builder()
                .schemaOfAdditionalProperties(new NumberSchema())
                .patternProperty("aa.*", BooleanSchema.INSTANCE)
                .build().validate(OBJECTS.get("patternPropertyOverridesAdditionalPropSchema"));
    }

    @Test
    public void patternPropertyViolation() {
        ObjectSchema subject = ObjectSchema.builder()
                .patternProperty("^b_.*", BooleanSchema.INSTANCE)
                .patternProperty("^s_.*", new StringSchema())
                .build();
        TestSupport.expectFailure(subject, BooleanSchema.INSTANCE, "#/b_1",
                OBJECTS.get("patternPropertyViolation"));
    }

    @Test
    public void patternPropsOverrideAdditionalProps() {
        ObjectSchema.builder()
                .patternProperty("^v.*", EmptySchema.INSTANCE)
                .additionalProperties(false)
                .build().validate(OBJECTS.get("patternPropsOverrideAdditionalProps"));
    }

    @Test
    public void propertyDepViolation() {
        ObjectSchema subject = buildWithLocation(
                ObjectSchema.builder()
                        .addPropertySchema("ifPresent", NullSchema.INSTANCE)
                        .addPropertySchema("mustBePresent", BooleanSchema.INSTANCE)
                        .propertyDependency("ifPresent", "mustBePresent")
        );
        TestSupport.failureOf(subject)
                .input(OBJECTS.get("propertyDepViolation"))
                .expectedKeyword("dependencies")
                .expect();
    }

    @Test
    public void propertySchemaViolation() {
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("boolProp", BooleanSchema.INSTANCE).build();
        TestSupport.expectFailure(subject, BooleanSchema.INSTANCE, "#/boolProp",
                OBJECTS.get("propertySchemaViolation"));
    }

    @Test
    public void requiredProperties() {
        ObjectSchema subject = buildWithLocation(
                ObjectSchema.builder()
                        .addPropertySchema("boolProp", BooleanSchema.INSTANCE)
                        .addPropertySchema("nullProp", NullSchema.INSTANCE)
                        .addRequiredProperty("boolProp")
        );
        TestSupport.failureOf(subject)
                .expectedPointer("#")
                .expectedKeyword("required")
                .input(OBJECTS.get("requiredProperties"))
                .expect();
    }

    @Test
    public void requireObject() {
        TestSupport.expectFailure(buildWithLocation(ObjectSchema.builder()), "#", "foo");
    }

    @Test
    public void schemaDepViolation() {
        Schema billingAddressSchema = new StringSchema();
        ObjectSchema subject = ObjectSchema.builder()
                .addPropertySchema("name", new StringSchema())
                .addPropertySchema("credit_card", NumberSchema.builder().build())
                .schemaDependency("credit_card", ObjectSchema.builder()
                        .addPropertySchema("billing_address", billingAddressSchema)
                        .addRequiredProperty("billing_address")
                        .build())
                .build();
        TestSupport.expectFailure(subject, billingAddressSchema, "#/billing_address",
                OBJECTS.get("schemaDepViolation"));
    }

    @Test
    public void schemaForNoAdditionalProperties() {
        assertThrows(SchemaException.class, () -> {
            ObjectSchema.builder().additionalProperties(false)
                    .schemaOfAdditionalProperties(BooleanSchema.INSTANCE).build();
        });
    }

    @Test
    public void testImmutability() {
        ObjectSchema.Builder builder = ObjectSchema.builder();
        builder.propertyDependency("a", "b");
        builder.schemaDependency("a", BooleanSchema.INSTANCE);
        builder.patternProperty("aaa", BooleanSchema.INSTANCE);
        ObjectSchema schema = builder.build();
        builder.propertyDependency("c", "a");
        builder.schemaDependency("b", BooleanSchema.INSTANCE);
        builder.patternProperty("bbb", BooleanSchema.INSTANCE);
        assertEquals(1, schema.getPropertyDependencies().size());
        assertEquals(1, schema.getSchemaDependencies().size());
        assertEquals(1, schema.getPatternProperties().size());
    }

    @Test
    public void typeFailure() {
        TestSupport.failureOf(ObjectSchema.builder())
                .expectedKeyword("type")
                .input("a")
                .expect();
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(ObjectSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation", "location")
                .withPrefabValues(Pattern.class, Pattern.compile("red"), Pattern.compile("black"))
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toStringNoExplicitType() {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema.json");
        rawSchemaJson.remove("type");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toStringWithUnprocessedProps() {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema-unprocessed.json");
        Schema schema = SchemaLoader.load(rawSchemaJson);
        String actual = schema.toString();
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toStringWithDefault() {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema-default.json");
        Schema schema = ObjectSchema.builder()
                .addPropertySchema("a", EmptySchema.builder()
                        .defaultValue(new JSONObject("{\"description\":\"default empty value\"}"))
                        .build())
                .build();
        String actual = schema.toString();
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toStringNoAdditionalProperties() {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema.json");
        rawSchemaJson.put("additionalProperties", false);
        Schema schema = SchemaLoader.load(rawSchemaJson);
        String actual = schema.toString();
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toStringSchemaDependencies() {
        JSONObject rawSchemaJson = loader.readObj("tostring/objectschema-schemadep.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void schemaPointerIsPassedToValidationException() {
        JSONPointer pointer = new JSONPointer(asList("dependencies", "a"));
        Schema subject = ObjectSchema.builder().requiresObject(true)
                .minProperties(1)
                .schemaLocation(pointer.toURIFragment()).build();
        try {
            subject.validate(1);
        } catch (ValidationException e) {
            assertEquals(pointer.toURIFragment(), e.getSchemaLocation());
        }
    }

    @Test
    public void propertyNamesFailure() {
        StringSchema propNameSchema = StringSchema.builder()
                .minLength(5)
                .maxLength(7)
                .schemaLocation("#/propertyNames")
                .build();
        ObjectSchema.Builder subject = ObjectSchema.builder()
                .propertyNameSchema(propNameSchema
                );

        TestSupport.failureOf(subject)
                .expectedViolatedSchema(propNameSchema)
                .expectedPointer("#/a")
                .expectedSchemaLocation("#/propertyNames")
                .input(new JSONObject("{\"a\":null}"))
                .expect();
    }

    @Test
    public void toStringWithPropertySchema() {
        JSONObject rawSchema = loader.readObj("tostring/objectschema-propertynames.json");
        Schema subject = loadAsV6(rawSchema);

        String actual = subject.toString();

        assertThat(new JSONObject(actual), sameJsonAs(rawSchema));
    }

    @Test
    public void emptyObjectPropertyNamesSchema() {
        Schema subject = ObjectSchema.builder().propertyNameSchema(StringSchema.builder().build()).build();

        subject.validate(new JSONObject("{}"));
    }

    @Test
    public void requiresObject_stillNullable() {
        ObjectSchema.builder().requiresObject(true).nullable(true).build().validate(JSONObject.NULL);
    }
}
