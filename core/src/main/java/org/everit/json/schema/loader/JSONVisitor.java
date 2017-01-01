package org.everit.json.schema.loader;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author erosb
 */
interface JSONVisitor<R> {

    static String requireString(JSONValue value) {
        return value.accept(TypeMatchingJSONVisitor.forType(String.class));
    }

    static <R> R requireString(JSONValue value, BiFunction<String, LoadingState, R> onSuccess) {
        return value.accept(new TypeMatchingJSONVisitor<>(String.class, onSuccess));
    }

    static List<JSONValue> requireArray(JSONValue value) {
        return value.accept(TypeMatchingJSONVisitor.forType(List.class));
    }

    static <R> R requireArray(JSONValue value, BiFunction<List<JSONValue>, LoadingState, R> onSuccess) {
        // I don't clearly understand why this wrapper function is needed
        BiFunction<List, LoadingState, R> rawOnSuccess = (e, ls) -> (R) onSuccess.apply(e, ls);
        return value.accept(new TypeMatchingJSONVisitor<>(List.class, rawOnSuccess));
    }

    static Boolean requireBoolean(JSONValue value) {
        return value.accept(TypeMatchingJSONVisitor.forType(Boolean.class));
    }

    static <R> R requireBoolean(JSONValue value, BiFunction<Boolean, LoadingState, R> onSuccess) {
        return value.accept(new TypeMatchingJSONVisitor<>(Boolean.class, onSuccess));
    }

    static Map<String, JSONValue> requireObject(JSONValue value) {
        return value.accept(TypeMatchingJSONVisitor.forType(Map.class));
    }

    static <R> R requireObject(JSONValue value, BiFunction<Map<String, JSONValue>, LoadingState, R> onSuccess) {
        BiFunction<Map, LoadingState, R> rawOnSuccess = (obj, ls) -> (R) onSuccess.apply(obj, ls);
        return value.accept(new TypeMatchingJSONVisitor<>(Map.class, rawOnSuccess));
    }

    R visitBoolean(Boolean value, LoadingState ls);

    R visitArray(List<JSONValue> value, LoadingState ls);

    R visitString(String value, LoadingState ls);

    R visitInteger(Integer value, LoadingState ls);

    R visitObject(Map<String, JSONValue> obj, LoadingState ls);

    R visitNull(LoadingState ls);

    R finishedVisiting(LoadingState ls);
}
