package org.everit.json.schema;

/**
 * @author erosb
 */
public class TrueSchema extends EmptySchema {

    public static class Builder extends EmptySchema.Builder {

        @Override public TrueSchema build() {
            return new TrueSchema(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public TrueSchema(Builder builder) {
        super(builder);
    }

    @Override public String toString() {
        return "true";
    }

}
