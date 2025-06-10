package org.everit.json.schema;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Arrays.asList;

/**
 * The methods of this class are copied from {@code org.json.JSONObject}.
 *
 * Although it would be possible to call {@code JSONObject#stringToValue()} from
 * {@link ValidatingVisitor#ifPassesTypeCheck(Class, Function, boolean, Boolean, Consumer)}, we can not do it,
 * because {@code JSONObject#stringToValue()} does not exist in the android flavor of the org.json package,
 * therefore on android it would throw a {@link NoSuchMethodError}. For that reason, these methods are copied
 * to the everit-org/json-schema library, to make sure that they exist at run-time.
 *
 * Furthermore, this implementation accepts all 22 boolean literals of YAML ( https://yaml.org/type/bool.html )
 * as valid booleans.
 */
class StringToValueConverter {

    private static final Set<String> YAML_BOOLEAN_TRUE_LITERALS = new HashSet<>(asList(
            "y",
            "Y",
            "yes",
            "Yes",
            "YES",
            "true",
            "True",
            "TRUE",
            "on",
            "On",
            "ON"
    ));

    private static final Set<String> YAML_BOOLEAN_FALSE_LITERALS = new HashSet<>(asList(
            "n",
            "N",
            "no",
            "No",
            "NO",
            "false",
            "False",
            "FALSE",
            "off",
            "Off",
            "OFF"
    ));



    static Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        }

        if (YAML_BOOLEAN_TRUE_LITERALS.contains(string)) {
            return Boolean.TRUE;
        }
        if (YAML_BOOLEAN_FALSE_LITERALS.contains(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return JSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it. If a number cannot be
         * produced, then the value will just be a string.
         */

        char initial = string.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            try {
                return stringToNumber(string);
            } catch (Exception ignore) {
            }
        }
        return string;
    }

    private static boolean isDecimalNotation(final String val) {

        //Applied the REFACTORING method: "Introduce explaining variable"
        /*creating a new variable to hold the result of a complex expression or calculation*/
        
        boolean containsDecimalPoint = val.indexOf('.') > -1;
        boolean containsExponent = val.indexOf('e') > -1 || val.indexOf('E') > -1;
        boolean isNegativeZero = "-0".equals(val);
        
        return containsDecimalPoint || containsExponent || isNegativeZero;

        // return val.indexOf('.') > -1 || val.indexOf('e') > -1
        //         || val.indexOf('E') > -1 || "-0".equals(val);
    }

    /*private static Number stringToNumber(final String val) throws NumberFormatException {
        char initial = val.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            // decimal representation
            if (isDecimalNotation(val)) {
                // Use a BigDecimal all the time so we keep the original
                // representation. BigDecimal doesn't support -0.0, ensure we
                // keep that by forcing a decimal.
                try {
                    BigDecimal bd = new BigDecimal(val);
                    if(initial == '-' && BigDecimal.ZERO.compareTo(bd)==0) {
                        return Double.valueOf(-0.0);
                    }
                    return bd;
                } catch (NumberFormatException retryAsDouble) {
                    // this is to support "Hex Floats" like this: 0x1.0P-1074
                    try {
                        Double d = Double.valueOf(val);
                        if(d.isNaN() || d.isInfinite()) {
                            throw new NumberFormatException("val ["+val+"] is not a valid number.");
                        }
                        return d;
                    } catch (NumberFormatException ignore) {
                        throw new NumberFormatException("val ["+val+"] is not a valid number.");
                    }
                }
            }
            // block items like 00 01 etc. Java number parsers treat these as Octal.
            if(initial == '0' && val.length() > 1) {
                char at1 = val.charAt(1);
                if(at1 >= '0' && at1 <= '9') {
                    throw new NumberFormatException("val ["+val+"] is not a valid number.");
                }
            } else if (initial == '-' && val.length() > 2) {
                char at1 = val.charAt(1);
                char at2 = val.charAt(2);
                if(at1 == '0' && at2 >= '0' && at2 <= '9') {
                    throw new NumberFormatException("val ["+val+"] is not a valid number.");
                }
            }
            // integer representation.
            // This will narrow any values to the smallest reasonable Object representation
            // (Integer, Long, or BigInteger)

            // BigInteger down conversion: We use a similar bitLenth compare as
            // BigInteger#intValueExact uses. Increases GC, but objects hold
            // only what they need. i.e. Less runtime overhead if the value is
            // long lived.
            BigInteger bi = new BigInteger(val);
            if(bi.bitLength() <= 31){
                return Integer.valueOf(bi.intValue());
            }
            if(bi.bitLength() <= 63){
                return Long.valueOf(bi.longValue());
            }
            return bi;
        }
        throw new NumberFormatException("val ["+val+"] is not a valid number.");
    }*/

    private static Number stringToNumber(final String val) throws NumberFormatException {
        //Applied the REFACTORING method: "Decompose conditional:"
        /*breaking down a long, complex conditional statement into smaller, more manageable pieces.*/
        char initial = val.charAt(0);
        if (isValidInitialChar(initial)) {
            if (isDecimalNotation(val)) {
                return parseDecimal(val, initial);
            }
            if (isInvalidOctalNumber(val)) {
                throw new NumberFormatException("val ["+val+"] is not a valid number.");
            }
            return parseInteger(val);
        }
        throw new NumberFormatException("val ["+val+"] is not a valid number.");
    }

    private static boolean isValidInitialChar(char initial) {
        return (initial >= '0' && initial <= '9') || initial == '-';
    }


    private static Number parseDecimal(String val, char initial) {
        // Use a BigDecimal all the time so we keep the original
        // representation. BigDecimal doesn't support -0.0, ensure we
        // keep that by forcing a decimal.
        try {
            BigDecimal bd = new BigDecimal(val);
            if (initial == '-' && BigDecimal.ZERO.compareTo(bd) == 0) {
                return Double.valueOf(-0.0);
            }
            return bd;
        } catch (NumberFormatException retryAsDouble) {
            // this is to support "Hex Floats" like this: 0x1.0P-1074
            try {
                Double d = Double.valueOf(val);
                if (d.isNaN() || d.isInfinite()) {
                    throw new NumberFormatException("val [" + val + "] is not a valid number.");
                }
                return d;
            } catch (NumberFormatException ignore) {
                throw new NumberFormatException("val [" + val + "] is not a valid number.");
            }
        }
    }

    private static boolean isInvalidOctalNumber(String val) {
        if (val.length() > 1 && val.charAt(0) == '0') {
            char at1 = val.charAt(1);
            if (at1 >= '0' && at1 <= '9') {
                return true;
            }
        } else if (val.length() > 2 && val.charAt(0) == '-' && val.charAt(1) == '0') {
            char at2 = val.charAt(2);
            if (at2 >= '0' && at2 <= '9') {
                return true;
            }
        }
        return false;
    }

    private static Number parseInteger(String val) {
        BigInteger bi = new BigInteger(val);
        if (bi.bitLength() <= 31) {
            return Integer.valueOf(bi.intValue());
        }
        if (bi.bitLength() <= 63) {
            return Long.valueOf(bi.longValue());
        }
        return bi;
    }

}
