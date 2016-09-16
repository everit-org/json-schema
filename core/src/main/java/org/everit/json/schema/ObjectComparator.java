/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

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

    private static boolean deepEqualArrays(final JSONArray arr1, final JSONArray arr2) {
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

    private static String[] sortedNamesOf(final JSONObject obj) {
        String[] raw = JSONObject.getNames(obj);
        if (raw == null) {
            return null;
        }
        Arrays.sort(raw, String.CASE_INSENSITIVE_ORDER);
        return raw;
    }

    private static boolean deepEqualObjects(final JSONObject jsonObj1, final JSONObject jsonObj2) {
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
