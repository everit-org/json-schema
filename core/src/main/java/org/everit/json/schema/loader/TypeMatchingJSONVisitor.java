package org.everit.json.schema.loader;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author erosb
 */
class TypeMatchingJSONVisitor<T, R> extends BaseJSONVisitor<R> {

    public static <T> JSONVisitor<T> forType(Class<T> expectedType) {
        return new TypeMatchingJSONVisitor(expectedType, (e, ls) -> e);
    }

    private final Class<T> expectedType;

    private Class<?> actualType;

    private T actualValue;

    private final BiFunction<T, LoadingState, R> onSuccess;

    TypeMatchingJSONVisitor(Class<T> expectedType, BiFunction<T, LoadingState, R> onSuccess) {
        this.expectedType = expectedType;
        this.onSuccess = onSuccess;
    }

    @Override public R visitBoolean(Boolean value, LoadingState ls) {
        actualType = Boolean.class;
        actualValue = (T) value;
        return null;
    }

    @Override public R visitArray(List value, LoadingState ls) {
        actualType = List.class;
        actualValue = (T) value;
        return null;
    }

    @Override public R visitString(String value, LoadingState ls) {
        actualType = String.class;
        actualValue = (T) value;
        return null;
    }

    @Override public R visitInteger(Integer value, LoadingState ls) {
        throw new UnsupportedOperationException("do we really need visitInteger() ?");
    }

    @Override public R visitObject(Map obj, LoadingState ls) {
        actualValue = (T) obj;
        return null;
    }

    @Override public R visitNull(LoadingState ls) {
        actualValue = null;
        return null;
    }

    @Override public R finishedVisiting(LoadingState ls) {
        if (expectedType == actualType) {
            return onSuccess.apply(actualValue, ls);
        } else {
            throw ls.createSchemaException(actualType, expectedType);
        }
    }
}
