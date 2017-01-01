package org.everit.json.schema;

import org.json.JSONPointer;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Thrown by {@link org.everit.json.schema.loader.SchemaLoader#load()} when it encounters
 * un-parseable schema JSON definition.
 *
 * @author @erosb
 */
public class SchemaException extends RuntimeException {

    private static final long serialVersionUID = 5987489689035036987L;

    private static Object typeOfValue(final Object actualValue) {
        return actualValue == null ? "null" : actualValue.getClass().getSimpleName();
    }

    static String buildMessage(JSONPointer pointer, Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        requireNonNull(pointer, "pointer cannot be null");
        String actualTypeDescr = actualType == null ? "null" : actualType.getSimpleName();
        String formattedPointer = pointer.toURIFragment().toString();
        if (furtherExpectedTypes != null && furtherExpectedTypes.length > 0) {
            Class<?>[] allExpecteds = new Class<?>[furtherExpectedTypes.length + 1];
            allExpecteds[0] = expectedType;
            System.arraycopy(furtherExpectedTypes, 0, allExpecteds, 1, furtherExpectedTypes.length);
            String expectedTypes = Arrays.stream(allExpecteds)
                    .map(Class::getSimpleName)
                    .collect(joining(" or "));
            return format("%s: expected type is one of %s, found: %s", formattedPointer,
                    expectedTypes,
                    actualTypeDescr);
        }
        return format("%s: expected type: %s, found: %s", formattedPointer,
                expectedType.getSimpleName(),
                actualTypeDescr);
    }

    private static String joinClassNames(final List<Class<?>> expectedTypes) {
        return expectedTypes.stream().map(Class::getSimpleName).collect(joining(", "));
    }

    private final JSONPointer pointerToViolation;

    public SchemaException(JSONPointer pointerToViolation, String message) {
        super(pointerToViolation.toURIFragment().toString() + ": " + message);
        this.pointerToViolation = pointerToViolation;
    }

    public SchemaException(JSONPointer pointerToViolation, Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        super(buildMessage(pointerToViolation, actualType, expectedType, furtherExpectedTypes));
        this.pointerToViolation = pointerToViolation;
    }

    @Deprecated
    public SchemaException(final String message) {
        this((JSONPointer) null, message);
    }

    @Deprecated
    public SchemaException(final String key, final Class<?> expectedType, final Object actualValue) {
        this(format("key %s : expected type: %s , found : %s", key, expectedType
                .getSimpleName(), typeOfValue(actualValue)));
    }

    @Deprecated
    public SchemaException(final String key, final List<Class<?>> expectedTypes,
            final Object actualValue) {
        this(format("key %s: expected type is one of %s, found: %s",
                key, joinClassNames(expectedTypes), typeOfValue(actualValue)));
    }

    @Deprecated
    public SchemaException(final String message, final Throwable cause) {
        super(message, cause);
        this.pointerToViolation = null;
    }

    public JSONPointer getPointerToViolation() {
        return pointerToViolation;
    }
}
