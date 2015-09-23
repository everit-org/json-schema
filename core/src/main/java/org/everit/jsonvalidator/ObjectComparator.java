package org.everit.jsonvalidator;

import java.util.Arrays;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Deep-equals implementation on primitive wrappers, {@link JSONObject} and {@link JSONArray}.
 */
public final class ObjectComparator {

  /**
   * Deep-equals implementation on primitive wrappers, {@link JSONObject} and {@link JSONArray}.
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

  private static boolean deepEqualObjects(final JSONObject jsonObj1, final JSONObject jsonObj2) {
    String[] obj1Names = JSONObject.getNames(jsonObj1);
    if (!Arrays.equals(obj1Names, JSONObject.getNames(jsonObj2))) {
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
