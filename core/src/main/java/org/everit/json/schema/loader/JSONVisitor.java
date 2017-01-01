package org.everit.json.schema.loader;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author erosb
 */
interface JSONVisitor<R> {

    static String requireString(JSONTraverser traverser) {
        return traverser.accept(TypeMatchingJSONVisitor.forType(String.class));
    }

    static <R> R requireString(JSONTraverser traverser, BiFunction<String, LoadingState, R> onSuccess) {
        return traverser.accept(new TypeMatchingJSONVisitor<>(String.class, onSuccess));
    }

    static List<JSONTraverser> requireArray(JSONTraverser traverser) {
        return traverser.accept(TypeMatchingJSONVisitor.forType(List.class));
    }

    static <R> R requireArray(JSONTraverser traverser, BiFunction<List<JSONTraverser>, LoadingState, R> onSuccess) {
        // I don't clearly understand why this wrapper function is needed
        BiFunction<List, LoadingState, R> rawOnSuccess = (e, ls) -> (R) onSuccess.apply(e, ls);
        return traverser.accept(new TypeMatchingJSONVisitor<>(List.class, rawOnSuccess));
    }

    static Boolean requireBoolean(JSONTraverser traverser) {
        return traverser.accept(TypeMatchingJSONVisitor.forType(Boolean.class));
    }

    static <R> R requireBoolean(JSONTraverser traverser, BiFunction<Boolean, LoadingState, R> onSuccess) {
        return traverser.accept(new TypeMatchingJSONVisitor<>(Boolean.class, onSuccess));
    }

    static Map<String, JSONTraverser> requireObject(JSONTraverser traverser) {
        return traverser.accept(TypeMatchingJSONVisitor.forType(Map.class));
    }

    static <R> R requireObject(JSONTraverser traverser, BiFunction<Map<String, JSONTraverser>, LoadingState, R> onSuccess) {
        BiFunction<Map, LoadingState, R> rawOnSuccess = (obj, ls) -> (R) onSuccess.apply(obj, ls);
        return traverser.accept(new TypeMatchingJSONVisitor<>(Map.class, rawOnSuccess));
    }

    R visitBoolean(Boolean value, LoadingState ls);

    R visitArray(List<JSONTraverser> value, LoadingState ls);

    R visitString(String value, LoadingState ls);

    R visitInteger(Integer value, LoadingState ls);

    R visitObject(Map<String, JSONTraverser> obj, LoadingState ls);

    R visitNull(LoadingState ls);

    R finishedVisiting(LoadingState ls);
}
