package org.everit.json.schema;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


class TypeChecker  {
    private static final List<Class<?>> INTEGRAL_TYPES = Arrays.asList(Integer.class, Long.class, BigInteger.class,
        AtomicInteger.class, AtomicLong.class);

    static boolean isAssignableFrom(Class<?> class1, Class<?> class2) {
        if (class1.equals(Integer.class)) {
            return INTEGRAL_TYPES.contains(class2);
        }
        return class1.isAssignableFrom(class2);
    }
}

