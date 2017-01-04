package org.everit.json.schema.loader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * @author erosb
 */
class JsonValue {

    private static final BiFunction<?, LoadingState, ?> IDENTITY = (e, ls) -> e;

    private static final <T, R> BiFunction<T, LoadingState, R> identity() {
        return (BiFunction<T, LoadingState, R>) IDENTITY;
    }

    private static Class<?> typeOfValue(final Object actualValue) {
        return actualValue == null ? null : actualValue.getClass();
    }

    static JsonValue of(Object obj, LoadingState emptyLs) {
        return new JsonValue(obj, emptyLs);
    }

    private final Object obj;

    private final LoadingState ls;

    public JsonValue(Object obj, LoadingState ls) {
        this.obj = obj;
        this.ls = requireNonNull(ls, "ls cannot be null");
    }

    public <R> R accept(JSONVisitor<R> jsonVisitor) {
        try {
            if (obj == null || obj == JSONObject.NULL) {
                return jsonVisitor.visitNull(ls);
            } else if (obj instanceof JSONArray) {
                JSONArray arr = (JSONArray) obj;
                List<JsonValue> list = IntStream.range(0, arr.length())
                        .mapToObj(i -> new JsonValue(arr.get(i), ls.childFor(i)))
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
                    Map<String, JsonValue> objMap = new HashMap<>(objPropNames.length);
                    Arrays.stream(objPropNames)
                            .forEach(key -> objMap.put(key, valueForKey(jsonObj, key)));
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

    private JsonValue valueForKey(JSONObject jsonObj, String key) {
        return new JsonValue(jsonObj.get(key), ls.childFor(key));
    }



    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        JsonValue that = (JsonValue) o;

        return obj != null ? obj.equals(that.obj) : that.obj == null;

    }

    @Override public int hashCode() {
        return obj != null ? obj.hashCode() : 0;
    }

    @Override public String toString() {
        return "JsonValue{" +
                "obj=" + obj +
                '}';
    }

    public String requireString() {
        return requireString(identity());
    }

    public <R> R requireString(BiFunction<String, LoadingState, R> mapper) {
        if (obj instanceof String) {
            return mapper.apply((String) obj, ls);
        }
        throw ls.createSchemaException(typeOfValue(obj), String.class);
    }

    public Boolean requireBoolean() {
        return requireBoolean(identity());
    }

    public <R> R requireBoolean(BiFunction<Boolean, LoadingState, R> mapper) {
        if (obj instanceof Boolean) {
            return mapper.apply((Boolean) obj, ls);
        }
        throw ls.createSchemaException(typeOfValue(obj), Boolean.class);
    }

    public JsonObject requireObject() {
        return requireObject(identity());
    }

    public <R> R requireObject(BiFunction<JsonObject, LoadingState, R> mapper) {
        if (this instanceof JsonObject) {
            return mapper.apply((JsonObject) this, ls);
        }
        throw ls.createSchemaException(typeOfValue(obj), JsonObject.class);
    }

}
