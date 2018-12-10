package org.everit.json.schema;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONObject;

public class JSONMatcher extends TypeSafeMatcher<JSONObject> {

    public static JSONMatcher sameJsonAs(JSONObject expected) {
        return new JSONMatcher(expected);
    }

    private final JSONObject expected;

    private JSONMatcher(JSONObject expected) {
        this.expected = expected;
    }

    @Override protected boolean matchesSafely(JSONObject item) {
        return ObjectComparator.deepEquals(expected, item);
    }

    @Override public void describeTo(Description description) {
        description.appendText(expected.toString(2));
    }

    @Override protected void describeMismatchSafely(JSONObject item, Description description) {
        description.appendText("was ").appendText(item.toString(2));
    }
}
