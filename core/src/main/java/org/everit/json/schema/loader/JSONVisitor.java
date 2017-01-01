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
        TypeMatchingJSONVisitor<List, R> visitor = new TypeMatchingJSONVisitor<>(List.class, rawOnSuccess);
        return traverser.accept(visitor);
    }

    static Boolean requireBoolean(JSONTraverser traverser) {
        return traverser.accept(TypeMatchingJSONVisitor.forType(Boolean.class));
    }

    static <R> R requireBoolean(JSONTraverser traverser, BiFunction<Boolean, LoadingState, R> onSuccess) {
        return traverser.accept(new TypeMatchingJSONVisitor<>(Boolean.class, onSuccess));
    }

    R visitBoolean(Boolean value, LoadingState ls);

    R visitArray(List<JSONTraverser> value, LoadingState ls);

    R visitString(String value, LoadingState ls);

    R visitInteger(Integer value, LoadingState ls);

    R visitObject(Map<String, JSONTraverser> obj, LoadingState ls);

    R visitNull(LoadingState ls);

    R finishedVisiting(LoadingState ls);
}
