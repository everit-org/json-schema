package org.everit.json.schema.loader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * @author erosb
 */
class JSONTraverser {

    private final Object obj;

    private final LoadingState ls;

    public JSONTraverser(Object obj, LoadingState ls) {
        this.obj = obj;
        this.ls = requireNonNull(ls, "ls cannot be null");
    }

    public <R> R accept(JSONVisitor<R> jsonVisitor) {
        try {
            if (obj == null || obj == JSONObject.NULL) {
                return jsonVisitor.visitNull(ls);
            } else if (obj instanceof JSONArray) {
                JSONArray arr = (JSONArray) obj;
                List<JSONTraverser> list = IntStream.range(0, arr.length())
                        .mapToObj(i -> new JSONTraverser(arr.get(i), ls.childFor(i)))
                        .collect(toList());
                return jsonVisitor.visitArray(list, ls);
            } else if (obj instanceof Boolean) {
                return jsonVisitor.visitBoolean((Boolean) obj, ls);
            } else if (obj instanceof String) {
                return jsonVisitor.visitString((String) obj, ls);
            } else if (obj instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) obj;
                String[] objPropNames = JSONObject.getNames(jsonObj);
                if (objPropNames == null) {
                    return jsonVisitor.visitObject(emptyMap(), ls);
                } else {
                    Map<String, JSONTraverser> objMap = new HashMap<>(objPropNames.length);
                    Arrays.stream(objPropNames)
                            .forEach(key -> objMap.put(key, traverserForKey(jsonObj, key)));
                    return jsonVisitor.visitObject(objMap, ls);
                }
            } else {
                throw new IllegalStateException("unsupported type");
            }
        } finally {
            R finishOverride = jsonVisitor.finishedVisiting(ls);
            if (finishOverride != null) {
                return finishOverride;
            }
        }
    }

    private JSONTraverser traverserForKey(JSONObject jsonObj, String key) {
        return new JSONTraverser(jsonObj.get(key), ls.childFor(key));
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        JSONTraverser that = (JSONTraverser) o;

        return obj != null ? obj.equals(that.obj) : that.obj == null;

    }

    @Override public int hashCode() {
        return obj != null ? obj.hashCode() : 0;
    }
}
