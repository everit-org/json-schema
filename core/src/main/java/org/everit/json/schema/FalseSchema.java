package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;

/**
 * @author erosb
 */
public class FalseSchema extends Schema {

    public static class Builder extends Schema.Builder<FalseSchema> {

        @Override public FalseSchema build() {
            return new FalseSchema(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructor.
     *
     * @param builder
     *         the builder containing the optional title, description and id attributes of the schema
     */
    public FalseSchema(Builder builder) {
        super(builder);
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitFalseSchema(this);
    }

    @Override
    public void describeTo(JSONPrinter writer) {
        writer.value(false);
    }

    @Override
    public String toString() {
        return "false";
    }
}
