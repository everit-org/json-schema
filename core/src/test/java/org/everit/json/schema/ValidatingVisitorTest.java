package org.everit.json.schema;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class ValidatingVisitorTest {

    private ValidationFailureReporter reporter;

    @Before
    public void before() {
        reporter = mock(ValidationFailureReporter.class);
    }

    @Test
    public void passesTypeCheck_otherType_noRequires() {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, false, null));
        verifyZeroInteractions(reporter);
    }

    @Test
    public void passesTypeCheck_otherType_requires() {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, null));
        verify(reporter).failure(JSONObject.class, "string");
    }

    @Test
    public void passesTypeCheck_otherType_nullPermitted_nullObject() {
        ValidatingVisitor subject = new ValidatingVisitor(JSONObject.NULL, reporter, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, Boolean.TRUE));
        verifyZeroInteractions(reporter);
    }

    @Test
    public void passesTypeCheck_otherType_nullPermitted_nullReference() {
        ValidatingVisitor subject = new ValidatingVisitor(null, reporter, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, Boolean.TRUE));
        verifyZeroInteractions(reporter);
    }

    @Test
    public void passesTypeCheck_nullPermitted_nonNullValue() {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, Boolean.TRUE));
        verify(reporter).failure(JSONObject.class, "string");
    }

    @Test
    public void passesTypeCheck_requiresType_nullableIsNull() {
        ValidatingVisitor subject = new ValidatingVisitor(null, reporter, null);
        assertFalse(subject.passesTypeCheck(JSONObject.class, true, null));
        verify(reporter).failure(JSONObject.class, null);
    }

    @Test
    public void passesTypeCheck_sameType() {
        ValidatingVisitor subject = new ValidatingVisitor("string", reporter, null);
        assertTrue(subject.passesTypeCheck(String.class, true, Boolean.TRUE));
        verifyZeroInteractions(reporter);
    }

}
