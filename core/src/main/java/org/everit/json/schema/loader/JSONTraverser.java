package org.everit.json.schema.loader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

/**
 * @author erosb
 */
public class JSONTraverser {

    private Object obj;

    public JSONTraverser(Object obj) {
        this.obj = obj;
    }

    public void accept(JSONVisitor jsonVisitor) {
        if (obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) obj;
            List<JSONTraverser> list = IntStream.range(0, arr.length())
                    .mapToObj(arr::get)
                    .map(JSONTraverser::new)
                    .collect(toList());
            jsonVisitor.visitArray(list);
        } else if (obj instanceof Boolean) {
            jsonVisitor.visitBoolean((Boolean) obj);
        } else if (obj instanceof String) {
            jsonVisitor.visitString((String) obj);
        } else if (obj instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) obj;
            String[] objPropNames = JSONObject.getNames(jsonObj);
            if (objPropNames == null) {
                jsonVisitor.visitObject(emptyMap());
            } else {
                Map<String, JSONTraverser> objMap = new HashMap<>(objPropNames.length);
                Arrays.stream(objPropNames).forEach(key -> objMap.put(key, new JSONTraverser(jsonObj.get(key))));
                jsonVisitor.visitObject(objMap);
            }
        }
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
