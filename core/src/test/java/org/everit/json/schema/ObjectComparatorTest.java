package org.everit.json.schema;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import static java.util.Arrays.asList;

import org.everit.json.schema.loader.JsonArray;
import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.JsonValue;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

@RunWith(JUnitParamsRunner.class)
public class ObjectComparatorTest {

    public static final JsonArray EMPTY_ARRAY = new JsonArray(new ArrayList());
    public static final JsonObject EMPTY_OBJECT = new JsonObject();

    private Object[][] failingCases() {
        return new Object[][] {
                { "array, null", EMPTY_ARRAY, null },
                { "array, object", EMPTY_ARRAY, EMPTY_OBJECT },
                { "object, null", EMPTY_OBJECT, null },
                { "arrays with different length", EMPTY_ARRAY, new JsonArray(asList("null")) },
                { "arrays with different elems", new JsonArray(asList(new String[]{"true", "false"})), new JsonArray(asList(new String[]{"false", "true"})) },
                { "objects with different length", EMPTY_OBJECT, JsonValue.of(JsonSchemaUtil.stringToNode("{\"a\":true}")) }
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
