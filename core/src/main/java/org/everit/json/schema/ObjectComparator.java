package org.everit.json.schema;

import java.util.Arrays;
import java.util.Objects;

import org.everit.json.schema.loader.JsonArray;
import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.NullJsonObject;

/**
 * Deep-equals implementation on primitive wrappers, {@link JSONObject} and {@link JSONArray}.
 */
public final class ObjectComparator {

    /**
     * Deep-equals implementation on primitive wrappers, {@link JSONObject} and {@link JSONArray}.
     *
     * @param obj1 the first object to be inspected
     * @param obj2 the second object to be inspected
     * @return {@code true} if the two objects are equal, {@code false} otherwise
     */
    public static boolean deepEquals(final Object obj1, final Object obj2) {
        if (obj1 instanceof JsonArray) {
            if (!(obj2 instanceof JsonArray)) {
                return false;
            }
            return deepEqualArrays((JsonArray) obj1, (JsonArray) obj2);
        } else if (obj1 instanceof JsonObject) {
            if (!(obj2 instanceof JsonObject)) {
                return false;
            }
            return deepEqualObjects((JsonObject) obj1, (JsonObject) obj2);
        }
        return Objects.equals(obj1, obj2);
    }

    private static boolean deepEqualArrays(final JsonArray arr1, final JsonArray arr2) {
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

    private static String[] sortedNamesOf(final JsonObject obj) {
        String[] raw = obj.getNames();
        if (raw == null) {
            return null;
        }
        Arrays.sort(raw, String.CASE_INSENSITIVE_ORDER);
        return raw;
    }

    private static boolean deepEqualObjects(final JsonObject jsonObj1, final JsonObject jsonObj2) {
    	
		if (jsonObj1 instanceof NullJsonObject) {
		    if (!(jsonObj2 instanceof NullJsonObject)) {
		        return false;
		    }
		}
        
	    if (jsonObj2 instanceof NullJsonObject) {
		    if (!(jsonObj1 instanceof NullJsonObject)) {
		        return false;
		    }
		}
		
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
