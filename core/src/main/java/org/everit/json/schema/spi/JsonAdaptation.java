package org.everit.json.schema.spi;

/**
 * A service that adapts JSON types for use in schema validation.
 * <p>
 * An adaption is responsible for adapting implementation-specific JSON object and array
 * structures to instances of the {@link JsonObjectAdapter} and {@link JsonArrayAdapter} types.
 * JSON scalar values are adapted to an intrinsic type (String, Boolean, Number).
 * <p>
 * An adaptation can assume that for a given validation operation, exactly one
 * adaptation is in use, and that any given {@link JsonAdapter} was produced by
 * the single adaptation that is in use.
 *
 * @param <T> the base type common to the types used in the underlying JSON implementation
 */
public interface JsonAdaptation<T> {

    /**
     * Gets the implementation-specific type that represents a JSON array.
     * @return array type
     */
    Class<?> arrayType();

    /**
     * Gets the implementation-specific type that represents a JSON object.
     * @return object type
     */
    Class<?> objectType();

    /**
     * Gets the types supported by this implementation.
     * @return array of supported types
     */
    Class<?>[] supportedTypes();

    /**
     * Given an arbitrary type, tests whether the type is recognized as an adaptable type
     * in this adaptation.
     * <p>
     * An implementation does not need to recognize the intrinsic types (String, Boolean, Number)
     * but must recognize its own types.
     *
     * @param type the subject type to test
     * @return {@code true} if {@code type} is an adaptable type
     */
    boolean isSupportedType(Class<?> type);

    /**
     * Given an arbitrary value, tests whether the value is logically equal to null in this
     * adaptation.
     * @param value the value to test.
     * @return {@code true} if {@code value == null} or if {@code value} is equal to the JSON
     *      null representation supported by this adaptation (if any)
     */
    boolean isNull(Object value);

    /**
     * Adapts the given value by applying an adaptation function based on the input value.
     * @param value the subject value to adapt
     * @return the adapted value which must be either
     *    (1) an intrinsic representation of an implementation-specific JSON scalar value
     *       (e.g. String, Boolean, Number)
     *    OR (2) a {@link JsonArrayAdapter} for an implementation-specific JSON array
     *    OR (3) a {@link JsonObjectAdapter} for an implementation-specific JSON object
     *    OR (4) the input {@code value} if the value is not a recognized
     *       implementation-specific type
     */
    Object adapt(Object value);

    /**
     * Inverts the adaptation function applied to the given value.
     * @param value the subject adapted value
     * @return the result of inverting any recognized adaptation in {@code value} OR
     *      {@code value} if no adaptation was recognized. If {@code value} is an instance of
     *      {@link JsonAdapter} the return value is generally the result of
     *      {@link JsonAdapter#unwrap()}. If {@code value} has an intrinsic type (String,
     *      Boolean, Number), the return value may be an implementation-specific representation
     *      of any of these types.
     */
    T invert(Object value);

}
