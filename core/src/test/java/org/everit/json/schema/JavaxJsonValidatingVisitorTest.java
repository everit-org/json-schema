/*
 * File created on Sep 12, 2018
 *
 * Copyright (c) 2018 Carl Harris, Jr
 * and others as noted
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.everit.json.schema.javax.json.Jsr353Adaptation;
import org.everit.json.schema.javax.json.Jsr374Adaptation;
import org.everit.json.schema.spi.JsonAdaptation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class JavaxJsonValidatingVisitorTest {

    private static final Jsr374Adaptation JSR_374_ADAPTATION = new Jsr374Adaptation();
    private static final Jsr353Adaptation JSR_353_ADAPTATION = new Jsr353Adaptation();

    private ValidationFailureReporter reporter;

    @Before
    public void before() {
        reporter = mock(ValidationFailureReporter.class);
    }

    public Object[] javaxJsonAdaptations() {
        return new Object[] { JSR_374_ADAPTATION, JSR_353_ADAPTATION };
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void passesTypeCheck_otherType_noRequires(JsonAdaptation<?> jsonAdaptation) {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null, jsonAdaptation);
        assertFalse(subject.passesTypeCheck(jsonAdaptation.objectType(), false, null));
        verifyZeroInteractions(reporter);
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void passesTypeCheck_otherType_requires(JsonAdaptation<?> jsonAdaptation) {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null, jsonAdaptation);
        assertFalse(subject.passesTypeCheck(jsonAdaptation.objectType(), true, null));
        verify(reporter).failure(JsonObject.class, "string");
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void passesTypeCheck_otherType_nullPermitted_nullObject(JsonAdaptation<?> jsonAdaptation) {
        ValidatingVisitor subject = new ValidatingVisitor(JsonValue.NULL, reporter, null, jsonAdaptation);
        assertFalse(subject.passesTypeCheck(jsonAdaptation.objectType(), true, Boolean.TRUE));
        verifyZeroInteractions(reporter);
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void passesTypeCheck_otherType_nullPermitted_nullReference(JsonAdaptation<?> jsonAdaptation) {
        ValidatingVisitor subject = new ValidatingVisitor(null, reporter, null, jsonAdaptation);
        assertFalse(subject.passesTypeCheck(jsonAdaptation.objectType(), true, Boolean.TRUE));
        verifyZeroInteractions(reporter);
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void passesTypeCheck_nullPermitted_nonNullValue(JsonAdaptation<?> jsonAdaptation) {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null, jsonAdaptation);
        assertFalse(subject.passesTypeCheck(jsonAdaptation.objectType(), true, Boolean.TRUE));
        verify(reporter).failure(jsonAdaptation.objectType(), "string");
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void passesTypeCheck_requiresType_nullableIsNull(JsonAdaptation<?> jsonAdaptation) {
        ValidatingVisitor subject = new ValidatingVisitor(null, reporter, null, jsonAdaptation);
        assertFalse(subject.passesTypeCheck(jsonAdaptation.objectType(), true, null));
        verify(reporter).failure(jsonAdaptation.objectType(), null);
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void passesTypeCheck_sameType(JsonAdaptation<?> jsonAdaptation) {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null, jsonAdaptation);
        assertTrue(subject.passesTypeCheck(String.class, true, Boolean.TRUE));
        verifyZeroInteractions(reporter);
    }

    public Object[] permittedTypes() {
        return new Object[] {
                new Object[] { "str" },
                new Object[] { 1 },
                new Object[] { 1L },
                new Object[] { 1.0 },
                new Object[] { 1.0f },
                new Object[] { new BigInteger("42") },
                new Object[] { new BigDecimal("42.3") },
                new Object[] { true },
                new Object[] { null },
                new Object[] { JsonValue.NULL },
                new Object[] { JsonValue.FALSE },
                new Object[] { JsonValue.TRUE },
                new Object[] { Json.createValue("str") },
                new Object[] { Json.createValue(1) },
                new Object[] { Json.createObjectBuilder().build() },
                new Object[] { Json.createArrayBuilder().build() },
        };
    }

    public Object[] notPermittedTypes() {
        return new Object[] {
                new Object[] { new ArrayList<String>() },
                new Object[] { new RuntimeException() }
        };
    }

    @Test
    @Parameters(method = "permittedTypes")
    public void permittedTypeSuccess(Object subject) {
        new ValidatingVisitor(subject, reporter, ReadWriteValidator.NONE, JSR_374_ADAPTATION);
    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters(method = "notPermittedTypes")
    public void notPermittedTypeFailure(Object subject) {
        new ValidatingVisitor(subject, reporter, ReadWriteValidator.NONE, JSR_374_ADAPTATION);
    }

}
