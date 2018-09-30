package org.everit.json.schema.spi;

/**
 * A adapter for a JSON structure type (array or object).
 *
 * @param <T> the base type common to the types used in the underlying JSON implementation
 */
public interface JsonAdapter<T> {

    /**
     * Gets the implementation-specific representation of the structure instance.
     * @return implementation-specific JSON structure representation; generally this is the
     *      delegate of the adapter
     */
    T unwrap();

}
