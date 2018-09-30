package org.everit.json.schema.spi;

import java.util.Map;

/**
 * An adapter for a JSON object.
 * <p>
 * This interface represents the contract between a provider's JSON object implementation
 * and a validator utilizing JSON Schema.
 */
public interface JsonObjectAdapter<T> extends JsonAdapter<T> {

    /**
     * Gets the length of the object.
     * @return the number of key-value pairs contained in the object
     */
    int length();

    /**
     * Gets an array containing the keys for the key-value pairs contained in the object.
     * @return array of keys for values that may be retrieved using {@link #get(String)).
     */
    String[] keys();

    /**
     * Tests whether a given key has a corresponding value in the object.
     * @param key the subject key
     * @return {@code true} if this object has a value that corresponds to {@code key}
     */
    boolean has(String key);

    /**
     * Retrieves the value associated with a given key from this object, applying adaptation
     * to implementation specific types as needed.
     * <p>
     * It is expected that the validator will always test for the presence of a key via
     * the {@link #has(String)} method, before attempting to retrieve it. Therefore it is
     * reasonable and expected that an implementation throw an unchecked exception when
     * {@code key} does not exist in object. However, it is not a requirement that an
     * exception be thrown in this circumstance.
     *
     * @param key the subject key
     * @return the value of the element or {@code null}
     */
    T get(String key);

    /**
     * Replaces any existing value associated with a given key in this object.
     *
     * @param key the subject key
     * @param value the value to store
     */
    void put(String key, T value);

    /**
     * Adapts the object to the {@link Map} interface.
     * @return map adaptation
     */
    Map<String, T> toMap();

}
