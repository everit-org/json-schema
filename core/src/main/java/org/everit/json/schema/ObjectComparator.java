package org.everit.json.schema;

import java.util.*;

import org.everit.json.schema.spi.JsonArrayAdapter;
import org.everit.json.schema.spi.JsonObjectAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Deep-equals implementation on primitive wrappers, array and object adapters, and
 * {@link JSONObject} and {@link JSONArray}. {@link JSONArray} and {@link JSONObject} are
 * included in order to support test cases without the need to first wrap in an adapter.
 *
 */
public final class ObjectComparator {

    /**
     * Deep-equals implementation on primitive wrappers, {@link JSONObject} and {@link JSONArray}.
     *
     * @param obj1
     *         the first object to be inspected
     * @param obj2
     *         the second object to be inspected
     * @return {@code true} if the two objects are equal, {@code false} otherwise
     */
    public static boolean deepEquals(Object obj1, Object obj2) {
        if (obj1 instanceof JsonArrayAdapter) {
            if (!(obj2 instanceof JsonArrayAdapter)) {
                return false;
            }
            return deepEqualArrays((JsonArrayAdapter) obj1, (JsonArrayAdapter) obj2);
        } else if (obj1 instanceof JsonObjectAdapter) {
            if (!(obj2 instanceof JsonObjectAdapter)) {
                return false;
            }
            return deepEqualObjects((JsonObjectAdapter) obj1, (JsonObjectAdapter) obj2);
        } else if (obj1 instanceof JSONArray) {  // continue to recognize the JSONArray type to support
            if (!(obj2 instanceof JSONArray)) {  // test cases that don't adapt org.json types
                return false;
            }
            return deepEqualArrays(new JSONArrayAdapter((JSONArray) obj1), new JSONArrayAdapter((JSONArray) obj2));
        } else if (obj1 instanceof JSONObject) { // continue to recognize the JSONArray type to support
            if (!(obj2 instanceof JSONObject)) { // test cases that don't adapt org.json types
                return false;
            }
            return deepEqualObjects(new JSONObjectAdapter((JSONObject) obj1), new JSONObjectAdapter((JSONObject) obj2));
        }

        return Objects.equals(obj1, obj2);
    }

    private static boolean deepEqualArrays(JsonArrayAdapter arr1, JsonArrayAdapter arr2) {
        if (arr1.length() != arr2.length()) {
            return false;
        }
        for (int i = 0; i < arr1.length(); ++i) {
            if (!deepEquals(arr1.get(i), arr2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static String[] sortedNamesOf(JsonObjectAdapter obj) {
        String[] raw = obj.keys();
        if (raw == null) {
            return null;
        }
        Arrays.sort(raw, String.CASE_INSENSITIVE_ORDER);
        return raw;
    }

    private static boolean deepEqualObjects(JsonObjectAdapter jsonObj1,
            JsonObjectAdapter jsonObj2) {
        String[] obj1Names = sortedNamesOf(jsonObj1);
        if (!Arrays.equals(obj1Names, sortedNamesOf(jsonObj2))) {
            return false;
        }
        if (obj1Names == null) {
            return true;
        }
        for (String name : obj1Names) {
            if (!deepEquals(jsonObj1.get(name), jsonObj2.get(name))) {
                return false;
            }
        }
        return true;
    }

    private ObjectComparator() {
    }

}
