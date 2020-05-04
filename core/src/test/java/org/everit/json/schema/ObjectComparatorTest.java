package org.everit.json.schema;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


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

    private Object[][] numbersEqualCases() {
        return new Object[][] {
            { "int 1, double 1.0", 1, 1.0d },
            { "int 1, long 1", 1, 1L },
            { "double 1.0, long 1", 1.0d, 1L },
            { "int 2, double 2.0", 2, 2.0d },
            { "int 3, double 3.00000", 3, 3.00000d },
            { "int -1, double -1.0", -1, -1.0d },
            { "double 1.0, double 1.000", 1.0d, 1.000d },
            { "big decimal, double 0.0001", new BigDecimal("0.000100000000000"), 0.0001d },
            { "big decimal, double 1.1", new BigDecimal("1.100000000000000"), 1.1d },
            { "big integer, big decimal", new BigInteger("18446744073709551616"), new BigDecimal("18446744073709551616.0") },
            { "big integers", new BigInteger("18446744073709551616"), new BigInteger("18446744073709551616") },
            { "big decimals", new BigDecimal("18446744073709551616.0"), new BigDecimal("18446744073709551616.0") },
            { "big decimal, double large value", new BigDecimal("1844674407370.000000"), 1844674407370.0d },
            { "long, big decimal", 184467440737L, new BigDecimal("184467440737.000000") },
        };
    }

    private Object[][] numbersNotEqualCases() {
        return new Object[][] {
            { "int 1, double 1.01", 1, 1.01d },
            { "double 1.1, long 1", 1.1d, 1L },
            { "int 2, double 2.1", 2, 2.1d },
            { "int 3, double 3.00001", 3, 3.00001d },
            { "int -1, double -1.1", -1, -1.1d },
            { "double 1.0, double 1.001", 1.0d, 1.001d },
            { "big integer, big decimal", new BigInteger("18446744073709551616"), new BigDecimal("18446744073709551616.1") },
            { "big integers", new BigInteger("18446744073709551616"), new BigInteger("18446744073709551617") },
            { "big integer, double", new BigInteger("18446744073709551616"), 1844674407370.0d },
            { "big decimals", new BigDecimal("18446744073709551616.0"), new BigDecimal("18446744073709551616.1") },
            { "big decimal, double", new BigDecimal("1844674407370.000001"), 1844674407370.0d },
            { "long, big decimal", 184467440737L, new BigDecimal("184467440737.000001") },
        };
    }

    @Test
    @Parameters(method = "failingCases")
    @TestCaseName("{0} (false)")
    public void array_Null_failure(String testcaseName, Object arg1, Object arg2) {
        assertFalse(ObjectComparator.deepEquals(arg1, arg2));
        assertFalse(ObjectComparator.deepEquals(arg2, arg1));
    }

    @Test
    @Parameters(method = "numbersEqualCases")
    @TestCaseName("{0} (numbers are equal)")
    public void numberComparationSuccess(String testcaseName, Object arg1, Object arg2) {
        assertTrue(ObjectComparator.deepEquals(arg1, arg2));
        assertTrue(ObjectComparator.deepEquals(arg2, arg1));
    }

    @Test
    @Parameters(method = "numbersNotEqualCases")
    @TestCaseName("{0} (numbers are not equal)")
    public void numberComparationFailure(String testcaseName, Object arg1, Object arg2) {
        assertFalse(ObjectComparator.deepEquals(arg1, arg2));
        assertFalse(ObjectComparator.deepEquals(arg2, arg1));
    }

}
