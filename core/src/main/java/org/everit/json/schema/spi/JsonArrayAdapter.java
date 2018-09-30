package org.everit.json.schema.spi;

import java.util.List;

/**
 * An adapter for a JSON array.
 * <p>
 * This interface represents the contract between a provider's JSON array implementation
 * and a validator utilizing JSON Schema.
 */
public interface JsonArrayAdapter<T> extends JsonAdapter<T> {

    /**
     * Gets the length of the array.
     * @return the number of elements in the array; i.e. the maximum value for the array
     *      index which can be used {@link #get(int)} to retrieve a value or with
     *      {@link #put(int, Object)} to store a value
     */
    int length();

    /**
     * Retrieves the element of the array at the given index.
     * <p>
     * The validator has no need to retrieve values beyond the bounds of the underlying
     * array. Therefore it is reasonable and expected that an implementation throw an
     * unchecked exception when {@code index} is outside of the bounds of the array.
     * However, it is not a requirement that an exception be thrown in this circumstance.
     *
     * @param index the index of the element to retrieve
     * @return the value of the element or {@code null} if no element
     *     exists at the specified index
     */
    T get(int index);

    /**
     * Stores an the element in the array at the given index replacing any existing element
     * at the same index. Adaptation from intrinsic types or {@link JsonAdapter} subtypes
     * is performed as needed on the input value.
     * <p>
     * The validator has no need to store values beyond the bounds of the underlying
     * array. Therefore it is reasonable and expected that an implementation throw an
     * unchecked exception when {@code index} is outside of the bounds of the array.
     * However, it is not a requirement that an exception be thrown in this circumstance.
     *
     * @param index index at which to store the element
     * @param value the value to store
     */
    void put(int index, T value);

    /**
     * Adapts this array to the {@link List} interface.
     * @return list adaptation
     */
    List<T> toList();

}
