package org.everit.json.schema.javax.json;

import org.everit.json.schema.spi.JsonAdaptation;
import org.everit.json.schema.spi.JsonAdapter;

import javax.json.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A {@link JsonAdaptation} for the standard Java JSON types in the {@code javax.json}
 * package.
 * <p>
 * The current specification for these types is part of the JSON-P specification
 * (JSR-374), which is part of the Java EE 8 platform. An earlier version of the JSON-P
 * specification (JSR-353) was used in the Java EE 7 platform.
 * <p>
 * Because the later specification provides some improvements to the API needed to support
 * these types with JSON schema validation, we want to use the later version where possible.
 * However, most Java EE 7 containers will have the older JSR-353 API.
 * <p>
 * Using the {@link #newInstance()} method, a user of this adaptation can get an instance
 * that uses the latest version of the JSON-P that is available at runtime.
 */
public abstract class JavaxJsonAdaptation implements JsonAdaptation<JsonValue>  {

    static final String JSR_374_ADAPTATION = "Jsr374Adaptation";
    static final String JSR_353_ADAPTATION = "Jsr353Adaptation";

    @Override
    public Class<?> arrayType() {
        return JsonArray.class;
    }

    @Override
    public Class<?> objectType() {
        return JsonObject.class;
    }

    @Override
    public Class<?>[] supportedTypes() {
        return new Class<?>[] { JsonValue.class };
    }

    /**
     * Creates a new instance of an adaptation for the standard JSON types using the
     * latest version of the JSON-P implementation that is available via either the
     * thread context class loader (if one is set on the calling thread) or from the
     * same class loader that loaded this class.
     *
     * @return adaptation instance for JSON-P data types
     * @throws RuntimeException if the adaptation class cannot be instantiated
     */
    public static JavaxJsonAdaptation newInstance() {
        if (Thread.currentThread().getContextClassLoader() != null) {
            return newInstance(Thread.currentThread().getContextClassLoader());
        }
        return newInstance(JavaxJsonAdaptation.class.getClassLoader());
    }

    /**
     * Creates a new instance of an adaptation for the standard JSON types using the
     * latest version of the JSON-P implementation that is available on the specified
     * class loader.
     * <p>
     * This method may be use in dynamic modular runtime environments such as those
     * provided by OSGi.
     *
     * @param classLoader the class loader to use to find the JSON-P API and
     *      implementation classes
     * @return adaptation instance for JSON-P data types
     * @throws RuntimeException if the adaptation class cannot be instantiated
     */
    public static JavaxJsonAdaptation newInstance(ClassLoader classLoader) {
        return newInstance(classLoader, determineProvider("createValue", String.class));
    }

    /**
     * Creates a new adaptation instance using the given class loader to load a
     * specific provider name.
     * @param classLoader source class loader for the JSON-P types
     * @param providerName name of the {@link JavaxJsonAdaptation} to load
     * @return adaptation instance
     * @throws RuntimeException an instance of the specified type cannot be instantiated
     */
    static JavaxJsonAdaptation newInstance(ClassLoader classLoader,
            String providerName) {
        try {
            return (JavaxJsonAdaptation) classLoader.loadClass(
                    providerClassName(providerName)).newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Determine the name of the adaptation provider class based on the availability
     * of a particular sentinel method in the {@link Json} class.
     * @param sentinelMethodName name of the method whose presence is to be checked
     * @param argTypes argument types for the sentinel method
     * @return adaptation provider name
     */
    static String determineProvider(String sentinelMethodName, Class<?>... argTypes) {
        try {
            Json.class.getMethod(sentinelMethodName, argTypes);
            return JSR_374_ADAPTATION;
        } catch (NoSuchMethodException ex) {
            return JSR_353_ADAPTATION;
        }
    }

    /**
     * Constructs the fully-qualified class name for a given named provider class.
     * <p>
     * It is assumed that all providers are subtypes of this abstract base type and
     * are located in the same package.
     *
     * @param providerName provider class name
     * @return fully qualified class name
     */
    private static String providerClassName(String providerName) {
        return JavaxJsonAdaptation.class.getPackage().getName() + "." + providerName;
    }

    @Override
    public boolean isSupportedType(Class<?> type) {
        return JsonValue.class.isAssignableFrom(type);
    }

    @Override
    public boolean isNull(Object value) {
        return value == null || value == JsonValue.NULL;
    }

    @Override
    public Object adapt(Object value) {
        if (value == JsonValue.NULL) {
            return null;
        } else if (value == JsonValue.TRUE) {
            return true;
        } else if (value == JsonValue.FALSE) {
            return false;
        } else if (value instanceof JsonString) {
            return ((JsonString) value).getString();
        } else if (value instanceof JsonNumber) {
            if (((JsonNumber) value).isIntegral()) {
                return ((JsonNumber) value).bigIntegerValue();
            } else {
                return ((JsonNumber) value).bigDecimalValue();
            }
        } else if (value instanceof JsonArray) {
            return new JavaxJsonArrayAdapter((JsonArray) value);
        } else if (value instanceof JsonObject) {
            return new JavaxJsonObjectAdapter((JsonObject) value);
        } else {
            return value;
        }
    }

    @Override
    public JsonValue invert(Object value) {
        if (value == null) {
            return JsonValue.NULL;
        }
        if (value instanceof JsonAdapter) {
            return (JsonValue) ((JsonAdapter) value).unwrap();
        } else if (value instanceof String) {
            return createValue((String) value);
        } else if (value instanceof Boolean) {
            return ((boolean) value) ? JsonValue.TRUE : JsonValue.FALSE;
        } else if (value instanceof Integer || value instanceof Byte || value instanceof Short) {
            return createValue((int) value);
        } else if (value instanceof Long) {
            return createValue((long) value);
        } else if (value instanceof Double || value instanceof Float) {
            return createValue((double) value);
        } else if (value instanceof BigInteger) {
            return createValue((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            return createValue((BigDecimal) value);
        } else {
            throw new AssertionError("unrecognized intrinsic type");
        }
    }

    /**
     * Creates a {@link JsonValue} representing the given string using a provider-specific
     * mechanism.
     * @param value the subject string value
     * @return JSON value instance
     */
    abstract JsonValue createValue(String value);

    /**
     * Creates a {@link JsonValue} representing the given integer using a provider-specific
     * mechanism.
     * @param value the subject integer value
     * @return JSON value instance
     */
    abstract JsonValue createValue(int value);

    /**
     * Creates a {@link JsonValue} representing the given long using a provider-specific
     * mechanism.
     * @param value the subject long value
     * @return JSON value instance
     */
    abstract JsonValue createValue(long value);

    /**
     * Creates a {@link JsonValue} representing the given double using a provider-specific
     * mechanism.
     * @param value the subject double value
     * @return JSON value instance
     */
    abstract JsonValue createValue(double value);

    /**
     * Creates a {@link JsonValue} representing the given big integer using a
     * provider-specific mechanism.
     * @param value the subject big integer value
     * @return JSON value instance
     */
    abstract JsonValue createValue(BigInteger value);

    /**
     * Creates a {@link JsonValue} representing the given big decimal using a
     * provider-specific mechanism.
     * @param value the subject big decimal value
     * @return JSON value instance
     */
    abstract JsonValue createValue(BigDecimal value);

}
