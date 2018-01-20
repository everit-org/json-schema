package org.everit.json.schema.internal;

import static org.everit.json.schema.internal.ValidatorTestSupport.assertFailure;
import static org.everit.json.schema.internal.ValidatorTestSupport.assertSuccess;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RelativeJsonPointerFormatValidatorTest {

    private static final RelativeJsonPointerFormatValidator SUBJECT = new RelativeJsonPointerFormatValidator();

    @Test
    public void formatNameTest() {
        assertEquals("relative-json-pointer", SUBJECT.formatName());
    }

    @Test
    public void onlyUpwardsStepCount() {
        assertSuccess("1", SUBJECT);
    }

    @Test
    public void upwardsStepCountWithJsonPointer() {
        assertSuccess("23/foo/bar", SUBJECT);
    }

    @Test
    public void upwardsStepCountWithHashmark() {
        assertSuccess("2#", SUBJECT);
    }

    @Test
    public void negativeUpwardsStepCount() {
        assertFailure("-123", SUBJECT, "[-123] is not a valid relative JSON Pointer");
    }

    @Test
    public void hashmarkIsNotTheLastChar() {
        assertFailure("3/ab/c#d", SUBJECT, "[3/ab/c#d] is not a valid relative JSON Pointer");
    }

    @Test
    public void nonIntegerBeginning() {
        assertFailure("abc/d/e/f", SUBJECT, "[abc/d/e/f] is not a valid relative JSON Pointer");
    }

    @Test
    public void upwardsStepCountFollowedByInvalidJsonPointer() {
        assertFailure("123asd~~b", SUBJECT, "[123asd~~b] is not a valid relative JSON Pointer");
    }

    @Test
    public void upwardsStepCountFollowedByURLFormJsonPointer() {
        assertFailure("123#/a/b", SUBJECT, "[123#/a/b] is not a valid relative JSON Pointer");
    }

}
