package org.everit.json.schema;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.List;

/**
 * Thrown by {@link org.everit.json.schema.loader.SchemaLoader#load()} when it encounters
 * un-parseable schema JSON definition.
 *
 * @author erosb
 */
public class SchemaException extends RuntimeException {

    private static final long serialVersionUID = 5987489689035036987L;

    private static Object typeOfValue(final Object actualValue) {
        return actualValue == null ? "null" : actualValue.getClass().getSimpleName();
    }

    static String buildMessage(String pointer, Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        requireNonNull(pointer, "pointer cannot be null");
        String actualTypeDescr = actualTypeDescr(actualType);
        if (furtherExpectedTypes != null && furtherExpectedTypes.length > 0) {
            Class<?>[] allExpecteds = new Class<?>[furtherExpectedTypes.length + 1];
            allExpecteds[0] = expectedType;
            System.arraycopy(furtherExpectedTypes, 0, allExpecteds, 1, furtherExpectedTypes.length);
            return buildMessage(pointer, actualTypeDescr, asList(allExpecteds));
        }
        return format("%s: expected type: %s, found: %s", pointer,
                expectedType.getSimpleName(),
                actualTypeDescr);
    }

    private static String actualTypeDescr(Class<?> actualType) {
        return actualType == null ? "null" : actualType.getSimpleName();
    }

    static String buildMessage(String formattedPointer, String actualTypeDescr, Collection<Class<?>> expectedTypes) {
        String fmtExpectedTypes = expectedTypes.stream()
                .map(Class::getSimpleName)
                .collect(joining(" or "));
        return format("%s: expected type is one of %s, found: %s", formattedPointer,
                fmtExpectedTypes,
                actualTypeDescr);
    }

    private static String buildMessage(String pointer, Class<?> actualType, Collection<Class<?>> expectedTypes) {
        return buildMessage(pointer, actualTypeDescr(actualType), expectedTypes);
    }

    private static String joinClassNames(final List<Class<?>> expectedTypes) {
        return expectedTypes.stream().map(Class::getSimpleName).collect(joining(", "));
    }

    private final String schemaLocation;

    public SchemaException(String schemaLocation, String message) {
        super(schemaLocation == null
                ? "<unknown location>: " + message
                : schemaLocation + ": " + message);
        this.schemaLocation = schemaLocation;
    }

    public SchemaException(String schemaLocation, Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        super(buildMessage(schemaLocation, actualType, expectedType, furtherExpectedTypes));
        this.schemaLocation = schemaLocation;
    }

    public SchemaException(String schemaLocation, Class<?> actualType, Collection<Class<?>> expectedTypes) {
        super(buildMessage(schemaLocation, actualType, expectedTypes));
        this.schemaLocation = schemaLocation;
    }

    @Deprecated
    public SchemaException(String message) {
        this((String) null, message);
    }

    @Deprecated
    public SchemaException(String key, Class<?> expectedType, Object actualValue) {
        this(format("key %s : expected type: %s , found : %s", key, expectedType
                .getSimpleName(), typeOfValue(actualValue)));
    }

    @Deprecated
    public SchemaException(String key, List<Class<?>> expectedTypes,
            final Object actualValue) {
        this(format("key %s: expected type is one of %s, found: %s",
                key, joinClassNames(expectedTypes), typeOfValue(actualValue)));
    }

    @Deprecated
    public SchemaException(String message, Throwable cause) {
        super(message, cause);
        this.schemaLocation = null;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SchemaException that = (SchemaException) o;

        return toString().equals(that.toString());
    }

    @Override public int hashCode() {
        return toString().hashCode();
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }
}
