package org.everit.json.schema.loader;

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

}
