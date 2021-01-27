package org.everit.json.schema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class NumberComparatorTest {
    private static Object[][] equalCases() {
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

    private static Object[][] notEqualCases() {
        return new Object[][] {
            { "int 1, double 1.01", 1, 1.01d },
            { "long 1, double 1.1", 1L, 1.1d },
            { "int 2, double 2.1", 2, 2.1d },
            { "int 3, double 3.00001", 3, 3.00001d },
            { "double -1.1, int -1", -1.1d, -1 },
            { "double 1.0, double 1.001", 1.0d, 1.001d },
            { "big integer, big decimal", new BigInteger("18446744073709551616"), new BigDecimal("18446744073709551616.1") },
            { "big integers", new BigInteger("18446744073709551616"), new BigInteger("18446744073709551617") },
            { "double, big integer", 1844674407370.0d, new BigInteger("18446744073709551616") },
            { "big decimals", new BigDecimal("18446744073709551616.0"), new BigDecimal("18446744073709551616.1") },
            { "double, big decimal,", 1844674407370.0d, new BigDecimal("1844674407370.000001") },
            { "long, big decimal", 184467440737L, new BigDecimal("184467440737.000001") },
        };
    }

    @ParameterizedTest(name = "{0} (numbers are equal)")
    @MethodSource("equalCases")
    public void numberComparationSuccess(String testcaseName, Number arg1, Number arg2) {
        assertTrue(NumberComparator.deepEquals(arg1, arg2));
        assertEquals(0, NumberComparator.compare(arg2, arg1));
        assertEquals(0, NumberComparator.compare(arg1, arg2));
    }

    @ParameterizedTest(name = "{0} (numbers are not equal)")
    @MethodSource("notEqualCases")
    public void numberComparationFailure(String testcaseName, Number arg1, Number arg2) {
        assertFalse(ObjectComparator.deepEquals(arg1, arg2));
        assertFalse(ObjectComparator.deepEquals(arg2, arg1));
        assertEquals(-1, NumberComparator.compare(arg1, arg2));
        assertEquals(1, NumberComparator.compare(arg2, arg1));
    }
}
