package org.everit.json.schema;

import static org.junit.Assert.assertFalse;

import org.everit.json.schema.spi.JsonArrayAdapter;
import org.everit.json.schema.spi.JsonObjectAdapter;
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

    private static final JsonArrayAdapter EMPTY_ARRAY_ADAPTER =
            new JSONArrayAdapter(new JSONArray());

    private static final JsonObjectAdapter EMPTY_OBJECT_ADAPTER =
            new JSONObjectAdapter(new JSONObject());

    private Object[][] failingCases() {
        return new Object[][] {
                { "array, null", EMPTY_ARRAY, null },
                { "array, object", EMPTY_ARRAY, EMPTY_OBJECT },
                { "object, null", EMPTY_OBJECT, null },
                { "arrays with different length", EMPTY_ARRAY, new JSONArray("[null]") },
                { "arrays with different elems", new JSONArray("[true, false]"), new JSONArray("[false, true]") },
                { "objects with different length", EMPTY_OBJECT, new JSONObject("{\"a\":true}") },
                { "array adapter, null", EMPTY_ARRAY_ADAPTER, null },
                { "array adapter, object adapter", EMPTY_ARRAY_ADAPTER, EMPTY_OBJECT_ADAPTER },
                { "object adapter, null", EMPTY_OBJECT_ADAPTER, null },
                { "array adapters with different length", EMPTY_ARRAY_ADAPTER, new JSONArrayAdapter(new JSONArray("[null]")) },
                { "array adapters with different elems", new JSONArrayAdapter(new JSONArray("[true, false]")), new JSONArrayAdapter(new JSONArray("[false, true]")) },
                { "object adapters with different length", EMPTY_OBJECT_ADAPTER, new JSONObjectAdapter(new JSONObject("{\"a\":true}")) },
                { "array, array adapter", EMPTY_ARRAY, EMPTY_ARRAY_ADAPTER },
                { "object, object adapter", EMPTY_OBJECT, EMPTY_OBJECT_ADAPTER },
                { "array, object adapter", EMPTY_ARRAY, EMPTY_OBJECT_ADAPTER },
                { "object, array adapter", EMPTY_OBJECT, EMPTY_ARRAY_ADAPTER },
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
