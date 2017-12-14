package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;

/**
 * {@code Null} schema validator.
 */
public class NullSchema extends Schema {

    /**
     * Builder class for {@link NullSchema}.
     */
    public static class Builder extends Schema.Builder<NullSchema> {

        @Override
        public NullSchema build() {
            return new NullSchema(this);
        }
    }

    public static final NullSchema INSTANCE = new NullSchema(builder());

    public static Builder builder() {
        return new Builder();
    }

    public NullSchema(final Builder builder) {
        super(builder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof NullSchema) {
            NullSchema that = (NullSchema) o;
            return that.canEqual(this) && super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NullSchema;
    }

    @Override void accept(Visitor visitor) {
        visitor.visitNullSchema(this);
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) {
        writer.key("type");
        writer.value("null");
    }
}
