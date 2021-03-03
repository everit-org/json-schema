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
import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.json.JSONObject;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

public class CombinedSchemaTest {

    private static final List<Schema> SUBSCHEMAS = asList(
            NumberSchema.builder().multipleOf(10).build(),
            NumberSchema.builder().multipleOf(3).build());

    @Test
    public void subschemasSort() {
        CombinedSchema subcombined1 = CombinedSchema
                .allOf(asList(BooleanSchema.INSTANCE))
                .build();

        CombinedSchema subcombined2 = CombinedSchema
                .allOf(asList(NullSchema.INSTANCE))
                .build();

        BooleanSchema booleanSchema1 = BooleanSchema.INSTANCE;
        BooleanSchema booleanSchema2 = BooleanSchema.INSTANCE;

        EmptySchema emptySchema1 = EmptySchema.INSTANCE;
        EmptySchema emptySchema2 = EmptySchema.INSTANCE;

        NullSchema nullSchema1 = NullSchema.INSTANCE;
        NullSchema nullSchema2 = NullSchema.INSTANCE;

        // subschemas of type CombinedSchema should bubble to the top,
        // all other schema types should remain in same order
        CombinedSchema subject = CombinedSchema.builder()
                .criterion(CombinedSchema.ALL_CRITERION)
                .subschema(nullSchema1)
                .subschema(emptySchema1)
                .subschema(booleanSchema1)
                .subschema(subcombined1)
                .subschema(booleanSchema2)
                .subschema(emptySchema2)
                .subschema(nullSchema2)
                .subschema(subcombined2)
                .isSynthetic(true)
                .build();

        Object[] subschemas = subject.getSubschemas().toArray();

        assertEquals(8, subschemas.length);
        assertEquals(subcombined1, subschemas[0]);
        assertEquals(subcombined2, subschemas[1]);
        assertEquals(nullSchema1, subschemas[2]);
        assertEquals(emptySchema1, subschemas[3]);
        assertEquals(booleanSchema1, subschemas[4]);
        assertEquals(booleanSchema2, subschemas[5]);
        assertEquals(emptySchema2, subschemas[6]);
        assertEquals(nullSchema2, subschemas[7]);
    }

    @Test
    public void allCriterionFailure() {
        assertThrows(ValidationException.class, () -> {
            CombinedSchema.ALL_CRITERION.validate(10, 1);
        });
    }

    @Test
    public void allCriterionSuccess() {
        CombinedSchema.ALL_CRITERION.validate(10, 10);
    }

    @Test
    public void anyCriterionFailure() {
        assertThrows(ValidationException.class, () -> {
            CombinedSchema.ANY_CRITERION.validate(10, 0);
        });
    }

    @Test
    public void anyCriterionSuccess() {
        CombinedSchema.ANY_CRITERION.validate(10, 1);
    }

    @Test
    public void anyOfInvalid() {
        assertThrows(ValidationException.class, () -> {
            CombinedSchema.anyOf(asList(
                    StringSchema.builder().maxLength(2).build(),
                    StringSchema.builder().minLength(4).build()))
                    .build().validate("foo");
        });
    }

    @Test
    public void factories() {
        CombinedSchema.allOf(asList(BooleanSchema.INSTANCE));
        CombinedSchema.anyOf(asList(BooleanSchema.INSTANCE));
        CombinedSchema.oneOf(asList(BooleanSchema.INSTANCE));
    }

    @Test
    public void oneCriterionFailure() {
        assertThrows(ValidationException.class, () -> {
            CombinedSchema.ONE_CRITERION.validate(10, 2);
        });
    }

    @Test
    public void oneCriterionSuccess() {
        CombinedSchema.ONE_CRITERION.validate(10, 1);
    }

    @Test
    public void validateAll() {
        TestSupport.failureOf(CombinedSchema.allOf(SUBSCHEMAS))
                .input(20)
                .expectedKeyword("allOf")
                .expect();
    }

    @Test
    public void validateAny() {
        TestSupport.failureOf(CombinedSchema.anyOf(SUBSCHEMAS))
                .input(5)
                .expectedKeyword("anyOf")
                .expect();
    }

    @Test
    public void validateOne() {
        TestSupport.failureOf(CombinedSchema.oneOf(SUBSCHEMAS))
                .input(30)
                .expectedKeyword("oneOf")
                .expect();
    }

    @Test
    public void reportCauses() {
        try {
            CombinedSchema.allOf(SUBSCHEMAS).build().validate(24);
            fail("did not throw exception");
        } catch (ValidationException e) {
            assertEquals(1, e.getCausingExceptions().size());
        }
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(CombinedSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation", "location")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() {
        CombinedSchema subject = CombinedSchema
                .allOf(asList(BooleanSchema.INSTANCE, NullSchema.INSTANCE))
                .description("descr")
                .build();
        JSONObject actual = new JSONObject(subject.toString());
        assertThat(actual, sameJsonAs(new JSONObject("{\"allOf\":["
                + BooleanSchema.INSTANCE.toString()
                + ", "
                + NullSchema.INSTANCE
                + "], \"description\":\"descr\"}")));
    }

    @Test
    public void toStringTest_withSynthetic() {
        CombinedSchema subject = CombinedSchema.builder().criterion(CombinedSchema.ALL_CRITERION)
                .subschema(BooleanSchema.INSTANCE)
                .subschema(EmptySchema.INSTANCE)
                .isSynthetic(true)
                .build();

        String actual = subject.toString();

        assertThat(new JSONObject(actual), sameJsonAs(new JSONObject(BooleanSchema.INSTANCE.toString())));
    }

    @Test
    public void oneOfEarlyFailureTest() {
        CombinedSchema subject = CombinedSchema
                .oneOf(asList(ObjectSchema.builder()
                                .addPropertySchema("bar", NumberSchema.builder().requiresInteger(true).build())
                                .addRequiredProperty("bar")
                                .build(),
                        ObjectSchema.builder()
                                .addPropertySchema("foo", StringSchema.builder().requiresString(true).build())
                                .addRequiredProperty("foo")
                                .build()))
                .build();
        Validator validator = Validator.builder().failEarly().build();
        validator.performValidation(subject, new JSONObject("{\"foo\":\"a\"}"));
        validator.performValidation(subject, new JSONObject("{\"bar\":2}"));
        TestSupport.failureOf(subject)
                .validator(validator)
                .input(new JSONObject("{\"bar\":\"quux\", \"foo\":2}"))
                .expectedSchemaLocation(null)
                .expect();
    }

}
