package org.everit.json.schema.loader;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author erosb
 */
interface JSONVisitor<R> {

    static String requireString(JsonValue value) {
        return value.accept(TypeMatchingJSONVisitor.forType(String.class));
    }

    static <R> R requireString(JsonValue value, BiFunction<String, LoadingState, R> onSuccess) {
        return value.accept(new TypeMatchingJSONVisitor<>(String.class, onSuccess));
    }

    static List<JsonValue> requireArray(JsonValue value) {
        return value.accept(TypeMatchingJSONVisitor.forType(List.class));
    }

    static <R> R requireArray(JsonValue value, BiFunction<List<JsonValue>, LoadingState, R> onSuccess) {
        // I don't clearly understand why this wrapper function is needed
        BiFunction<List, LoadingState, R> rawOnSuccess = (e, ls) -> (R) onSuccess.apply(e, ls);
        return value.accept(new TypeMatchingJSONVisitor<>(List.class, rawOnSuccess));
    }

    static Boolean requireBoolean(JsonValue value) {
        return value.accept(TypeMatchingJSONVisitor.forType(Boolean.class));
    }

    static <R> R requireBoolean(JsonValue value, BiFunction<Boolean, LoadingState, R> onSuccess) {
        return value.accept(new TypeMatchingJSONVisitor<>(Boolean.class, onSuccess));
    }

    static Map<String, JsonValue> requireObject(JsonValue value) {
        return value.accept(TypeMatchingJSONVisitor.forType(Map.class));
    }

    static <R> R requireObject(JsonValue value, BiFunction<Map<String, JsonValue>, LoadingState, R> onSuccess) {
        BiFunction<Map, LoadingState, R> rawOnSuccess = (obj, ls) -> (R) onSuccess.apply(obj, ls);
        return value.accept(new TypeMatchingJSONVisitor<>(Map.class, rawOnSuccess));
    }

    R visitBoolean(Boolean value, LoadingState ls);

    R visitArray(List<JsonValue> value, LoadingState ls);

    R visitString(String value, LoadingState ls);

    R visitInteger(Integer value, LoadingState ls);

    R visitObject(Map<String, JsonValue> obj, LoadingState ls);

    R visitNull(LoadingState ls);

    R finishedVisiting(LoadingState ls);
}
