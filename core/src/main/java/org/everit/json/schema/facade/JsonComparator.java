package org.everit.json.schema.facade;

import org.json.JSONArray;

/**
 * Deep-equals implementation on primitive wrappers, {@link JsonObject} and {@link JSONArray}.
 */
public interface JsonComparator {
    /**
     * Deep-equals implementation on primitive wrappers, {@link JsonObject} and {@link JSONArray}.
     *
     * @param obj1 the first object to be inspected
     * @param obj2 the second object to be inspected
     * @return {@code true} if the two objects are equal, {@code false} otherwise
     */
    boolean deepEquals(Object obj1, Object obj2);
}
