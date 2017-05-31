package org.everit.json.schema;

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

    /**
     * Always throws {@link ValidationException}
     * @param subject the object to be validated
     */
    @Override public void validate(Object subject) {
        throw failure("false schema always fails", "false");
    }

    @Override public String toString() {
        return "false";
    }
}
