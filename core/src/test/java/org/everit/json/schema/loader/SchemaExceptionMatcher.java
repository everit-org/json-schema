package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author erosb
 */
public class SchemaExceptionMatcher extends TypeSafeMatcher<SchemaException> {

    public static final SchemaExceptionMatcher excWithPointer(String expectedPointer) {
        return new SchemaExceptionMatcher(expectedPointer);
    }

    private String expectedPointerToViolation;

    public SchemaExceptionMatcher(String expectedPointerToViolation) {
        this.expectedPointerToViolation = expectedPointerToViolation;
    }

    @Override public void describeTo(Description descr) {
        descr.appendText(expectedPointerToViolation);
    }

    @Override protected void describeMismatchSafely(SchemaException item, Description mismatchDescription) {
        mismatchDescription.appendText(item.getPointerToViolation().toString());
    }

    @Override protected boolean matchesSafely(SchemaException item) {
        return item.getPointerToViolation().toString().equals(expectedPointerToViolation);
    }

}
