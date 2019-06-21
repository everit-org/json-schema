package org.everit.json.schema;

import static org.everit.json.schema.loader.OrgJsonUtil.getNames;

import java.util.Arrays;
import java.util.Objects;

import org.json2.JSONArray;
import org.json2.JSONObject;

/**
 * Deep-equals implementation on primitive wrappers, {@link JSONObject} and {@link JSONArray}.
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
        if (obj1 instanceof JSONArray) {
            if (!(obj2 instanceof JSONArray)) {
                return false;
            }
            return deepEqualArrays((JSONArray) obj1, (JSONArray) obj2);
        } else if (obj1 instanceof JSONObject) {
            if (!(obj2 instanceof JSONObject)) {
                return false;
            }
            return deepEqualObjects((JSONObject) obj1, (JSONObject) obj2);
        }
        return Objects.equals(obj1, obj2);
    }

    private static boolean deepEqualArrays(JSONArray arr1, JSONArray arr2) {
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

    private static String[] sortedNamesOf(JSONObject obj) {
        String[] raw = getNames(obj);
        if (raw == null) {
            return null;
        }
        Arrays.sort(raw, String.CASE_INSENSITIVE_ORDER);
        return raw;
    }

    private static boolean deepEqualObjects(JSONObject jsonObj1, JSONObject jsonObj2) {
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
