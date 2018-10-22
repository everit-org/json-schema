package org.everit.json.schema;

import org.everit.json.schema.facade.Facade;
import org.json.JSONArray;

/**
 * Deep-equals implementation on primitive wrappers,
 * {@link org.everit.json.schema.facade.JsonObject} and
 * {@link JSONArray}.
 */
@Deprecated
public final class ObjectComparator {
    /**
     * Deep-equals implementation on primitive wrappers,
     * {@link org.everit.json.schema.facade.JsonObject} and
     * {@link JSONArray}.
     *
     * @param obj1 the first object to be inspected
     * @param obj2 the second object to be inspected
     * @return {@code true} if the two objects are equal, {@code false} otherwise
     */
    public static boolean deepEquals(Object obj1, Object obj2) {
        return Facade.getInstance().comparator()
            .deepEquals(obj1, obj2);
    }

    private ObjectComparator() {
    }
}
