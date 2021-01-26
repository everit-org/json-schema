package org.everit.json.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;


public class NumberComparator {

    static BigDecimal getAsBigDecimal(Object number) {
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger) number);
        } else if (number instanceof Integer || number instanceof Long) {
            return new BigDecimal(((Number) number).longValue());
        } else {
            double d = ((Number) number).doubleValue();
            return BigDecimal.valueOf(d);
        }
    }

    static boolean deepEquals(Number num1, Number num2) {
        if (num1.getClass() != num2.getClass()) {
            return compare(num1, num2) == 0;
        }
        return Objects.equals(num1, num2);
    }

    static int compare(Number num1, Number num2) {
        return getAsBigDecimal(num1).compareTo(getAsBigDecimal(num2));
    }

}
