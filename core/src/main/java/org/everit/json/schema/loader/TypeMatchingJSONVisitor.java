package org.everit.json.schema.loader;

/**
 * @author erosb
 */
public class TypeMatchingJSONVisitor extends BaseJSONVisitor {

    private Class<?> expectedType;

    public TypeMatchingJSONVisitor(Class<?> expectedType) {
        this.expectedType = expectedType;
    }

}
