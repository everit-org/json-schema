package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;

/**
 * @author erosb
 */
class TypeMatchingJSONVisitor extends BaseJSONVisitor {

    public static <T> JSONVisitor<T> forType(Class<T> expectedType) {
        return new TypeMatchingJSONVisitor(expectedType);
    }

    private Class<?> expectedType;

    public TypeMatchingJSONVisitor(Class<?> expectedType) {
        this.expectedType = expectedType;
    }

    @Override public Object finishedVisiting(LoadingState ls) {
        throw new SchemaException("asd");
    }
}
