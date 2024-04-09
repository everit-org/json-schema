package org.everit.json.schema;

import org.everit.json.schema.StringToValueConverter;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class StringToValueConverterTest {

    @Test
    public void testStringToValueWithEmptyString() {
        Object result = StringToValueConverter.stringToValue("");
        assertEquals("", result);
    }

    @Test
    public void testStringToValueWithBooleanTrueLiterals() {
        assertTrue((Boolean) StringToValueConverter.stringToValue("y"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("Y"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("yes"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("Yes"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("YES"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("true"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("True"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("TRUE"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("on"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("On"));
        assertTrue((Boolean) StringToValueConverter.stringToValue("ON"));
    }

    @Test
    public void testStringToValueWithBooleanFalseLiterals() {
        assertFalse((Boolean) StringToValueConverter.stringToValue("n"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("N"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("no"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("No"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("NO"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("false"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("False"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("FALSE"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("off"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("Off"));
        assertFalse((Boolean) StringToValueConverter.stringToValue("OFF"));
    }

    @Test
    public void testStringToValueWithNullLiteral() {
        Object result = StringToValueConverter.stringToValue("null");
        assertEquals(JSONObject.NULL, result);
    }

    @Test
    public void testStringToValueWithInteger() {
        Object result = StringToValueConverter.stringToValue("42");
        assertTrue(result instanceof Integer);
        assertEquals(42, result);
    }


    @Test
    public void testStringToValueWithBigInteger() {
        Object result = StringToValueConverter.stringToValue("9223372036854775809000"); // BigInteger value
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("9223372036854775809000"), result);
    }

    @Test
    public void testStringToValueWithBigDecimal() {
        Object result = StringToValueConverter.stringToValue("3.14159265359");
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("3.14159265359"), result);
    }

    @Test
    public void testStringToValueWithNegativeZero() {
        Object result = StringToValueConverter.stringToValue("-0.0");
        assertTrue(result instanceof Double);
        assertEquals(-0.0, result);
    }
}
