package org.everit.json.schema.loader;

import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;

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
