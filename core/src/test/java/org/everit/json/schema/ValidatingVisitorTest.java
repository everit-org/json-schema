package org.everit.json.schema;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import org.everit.json.schema.event.CombinedSchemaMatchEvent;
import org.everit.json.schema.event.CombinedSchemaMismatchEvent;
import org.everit.json.schema.event.ValidationListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class ValidatingVisitorTest {

    private ValidationFailureReporter reporter;

    @Before
    public void before() {
        reporter = mock(ValidationFailureReporter.class);
    }

    @Test
    public void passesTypeCheck_otherType_noRequires() {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, false, null));
        verifyZeroInteractions(reporter);
    }

    @Test
    public void passesTypeCheck_otherType_requires() {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, null));
        verify(reporter).failure(JSONObject.class, "string");
    }

    @Test
    public void passesTypeCheck_otherType_nullPermitted_nullObject() {
        ValidatingVisitor subject = new ValidatingVisitor(JSONObject.NULL, reporter, null, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, Boolean.TRUE));
        verifyZeroInteractions(reporter);
    }

    @Test
    public void passesTypeCheck_otherType_nullPermitted_nullReference() {
        ValidatingVisitor subject = new ValidatingVisitor(null, reporter, null, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, Boolean.TRUE));
        verifyZeroInteractions(reporter);
    }

    @Test
    public void passesTypeCheck_nullPermitted_nonNullValue() {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, Boolean.TRUE));
        verify(reporter).failure(JSONObject.class, "string");
    }

    @Test
    public void passesTypeCheck_requiresType_nullableIsNull() {
        ValidatingVisitor subject = new ValidatingVisitor(null, reporter, null, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, null));
        verify(reporter).failure(JSONObject.class, null);
    }

    @Test
    public void passesTypeCheck_sameType() {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null, null);
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
                new Object[] { JSONObject.NULL },
                new Object[] { new JSONObject("{}") },
                new Object[] { new JSONArray("[]") },
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
        new ValidatingVisitor(subject, reporter, ReadWriteValidator.NONE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters(method = "notPermittedTypes")
    public void notPermittedTypeFailure(Object subject) {
        new ValidatingVisitor(subject, reporter, ReadWriteValidator.NONE, null);
    }

    @Test
    public void triggersCombinedSchemaEvents() {
        ValidationListener listener = mock(ValidationListener.class);
        StringSchema stringSchema = StringSchema.builder().requiresString(true).build();
        EmptySchema emptySchema = EmptySchema.builder().build();
        ObjectSchema objectSchema = ObjectSchema.builder().requiresObject(true).build();
        CombinedSchema combinedSchema = CombinedSchema.builder().criterion(CombinedSchema.ONE_CRITERION)
                .subschema(stringSchema)
                .subschema(emptySchema)
                .subschema(objectSchema)
                .build();
        ValidationFailureReporter reporter = new CollectingFailureReporter(combinedSchema);
        JSONObject instance = new JSONObject();

        new ValidatingVisitor(instance, reporter, ReadWriteValidator.NONE, listener).visit(combinedSchema);

        ValidationException exc = new ValidationException(stringSchema, String.class, instance);
        verify(listener).combinedSchemaMismatch(new CombinedSchemaMismatchEvent(combinedSchema, stringSchema, instance, exc));
        verify(listener).combinedSchemaMatch(new CombinedSchemaMatchEvent(combinedSchema, emptySchema, instance));
        verify(listener).combinedSchemaMatch(new CombinedSchemaMatchEvent(combinedSchema, objectSchema, instance));
    }

}
