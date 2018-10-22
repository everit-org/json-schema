package org.everit.json.schema.facade.orgjson;

import org.everit.json.schema.facade.JsonComparator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

// TODO: New API (JsonObject)
// TODO: New API (JsonArray)
// Its fine to use JSON Array Here for now though, as they are extended anyway.
final class Comparator implements JsonComparator {
    @Override
    public boolean deepEquals(Object obj1, Object obj2) {
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

    private boolean deepEqualArrays(JSONArray arr1, JSONArray arr2) {
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

    private String[] sortedNamesOf(JSONObject obj) {
        String[] raw = JSONObject.getNames(obj);
        if (raw == null) {
            return null;
        }
        Arrays.sort(raw, String.CASE_INSENSITIVE_ORDER);
        return raw;
    }

    private boolean deepEqualObjects(JSONObject jsonObj1, JSONObject jsonObj2) {
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
}
