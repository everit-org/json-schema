package org.everit.json.schema;

import static org.junit.Assert.assertFalse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

@RunWith(JUnitParamsRunner.class)
public class ObjectComparatorTest {

    public static final JSONArray EMPTY_ARRAY = new JSONArray();
    public static final JSONObject EMPTY_OBJECT = new JSONObject();

    private Object[][] failingCases() {
        return new Object[][] {
                { "array, null", EMPTY_ARRAY, null },
                { "array, object", EMPTY_ARRAY, EMPTY_OBJECT },
                { "object, null", EMPTY_OBJECT, null },
                { "arrays with different length", EMPTY_ARRAY, new JSONArray("[null]") },
                { "arrays with different elems", new JSONArray("[true, false]"), new JSONArray("[false, true]") },
                { "objects with different length", EMPTY_OBJECT, new JSONObject("{\"a\":true}") }
        };
    }

    @Test
    @Parameters(method = "failingCases")
    @TestCaseName("{0} (false)")
    public void array_Null_failure(String testcaseName, Object arg1, Object arg2) {
        assertFalse(ObjectComparator.deepEquals(arg1, arg2));
        assertFalse(ObjectComparator.deepEquals(arg2, arg1));
    }

}
