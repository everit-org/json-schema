package org.everit.json.schema.loader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class JsonValue {

    private static final Function<?, ?> IDENTITY = e -> e;

    private static final <T, R> Function<T,  R> identity() {
        return (Function<T, R>) IDENTITY;
    }

    static JsonValue of(Object obj, LoadingState ls) {
        if (obj instanceof Map) {
            return new JsonObject((Map<String, Object>) obj, ls);
        } else if (obj instanceof List) {
            return new JsonArray((List<Object>) obj, ls);
        } else if (obj instanceof JSONObject) {
            JSONObject jo = (JSONObject) obj;
            return new JsonObject(jo.toMap(), ls);
        } else if (obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) obj;
            return new JsonArray(arr.toList(), ls);
        }
        return new JsonValue(obj, ls);
    }

    private final Object obj;

    protected final LoadingState ls;

    protected JsonValue(Object obj, LoadingState ls) {
        this.obj = obj;
        this.ls = requireNonNull(ls, "ls cannot be null");
    }

    protected Class<?> typeOfValue() {
        return obj == null ? null : obj.getClass();
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

    public <R> R requireString(Function<String, R> mapper) {
        if (obj instanceof String) {
            return mapper.apply((String) obj);
        }
        throw ls.createSchemaException(typeOfValue(), String.class);
    }

    public Boolean requireBoolean() {
        return requireBoolean(identity());
    }

    public <R> R requireBoolean(Function<Boolean, R> mapper) {
        if (obj instanceof Boolean) {
            return mapper.apply((Boolean) obj);
        }
        throw ls.createSchemaException(typeOfValue(), Boolean.class);
    }

    public JsonObject requireObject() {
        return requireObject(identity());
    }

    public <R> R requireObject(Function<JsonObject, R> mapper) {
        throw ls.createSchemaException(typeOfValue(), JsonObject.class);
    }

    public JsonArray requireArray() {
        return requireArray(identity());
    }

    public <R> R requireArray(Function<JsonArray, R> mapper) {
        throw ls.createSchemaException(typeOfValue(), JsonArray.class);
    }

    public Number requireNumber() {
        return requireNumber(identity());
    }

    public <R> R requireNumber(Function<Number, R> mapper) {
        if (obj instanceof Number) {
            return mapper.apply((Number) obj);
        }
        throw ls.createSchemaException(typeOfValue(), Number.class);
    }

}
