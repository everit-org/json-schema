package org.everit.json.schema.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class simplifying working with org.json.JSONObject and JSONArray in a way that it also works
 * on android.
 */
public class OrgJsonUtil {

    /**
     * Used as a replacement of {@code JSONObject#toMap()} (which doesn't exist in the android version of org.json).
     */
    public static Map<String, Object> toMap(JSONObject obj) {
        Map<String, Object> rval = new HashMap<>(obj.length());
        Iterator<String> keyIt = obj.keys();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            Object rawValue = obj.get(key);
            Object convertedValue = convertValue(rawValue);
            rval.put(key, convertedValue);
        }
        return rval;
    }

    static Object convertValue(Object rawValue) {
        Object convertedValue;
        if (rawValue instanceof JSONObject) {
            convertedValue = toMap((JSONObject) rawValue);
        } else if (rawValue instanceof JSONArray) {
            convertedValue = toList((JSONArray) rawValue);
        } else {
            convertedValue = rawValue;
        }
        return convertedValue;
    }

    /**
     * Used as a replacement of {@code JSONArray#toList()} (which doesn't exist in the android version of org.json).
     */
    public static List<Object> toList(JSONArray arr) {
        List<Object> rval = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); ++i) {
            rval.add(convertValue(arr.get(i)));
        }
        return rval;
    }

    public static String[] getNames(JSONObject obj) {
        if (obj == null || obj.length() == 0) {
            return null;
        }
        String[] rval = new String[obj.length()];
        Iterator<String> keyIt = obj.keys();
        int idx = 0;
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            rval[idx++] = key;
        }
        return rval;
    }

    private OrgJsonUtil() {
    }

}
