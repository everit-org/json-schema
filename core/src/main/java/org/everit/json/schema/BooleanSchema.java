package org.everit.json.schema;

import java.io.StringWriter;

import org.everit.json.schema.internal.JSONPrinter;

/**
 * Boolean schema validator.
 */
public class BooleanSchema extends Schema {

    /**
     * Builder class for {@link BooleanSchema}.
     */
    public static class Builder extends Schema.Builder<BooleanSchema, Builder> {

        @Override
        protected Builder getBuilder()
        {
            return this;
        }

        @Override
        public BooleanSchema build() {
            return new BooleanSchema(this);
        }

    }

    public static final BooleanSchema INSTANCE = new BooleanSchema(builder());

    public static Builder builder() {
        return new Builder();
    }

    public BooleanSchema(final Builder builder) {
        super(builder);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BooleanSchema) {
            BooleanSchema that = (BooleanSchema) o;
            return that.canEqual(this) && super.equals(that);
        } else {
            return false;
        }
    }

    @Override void accept(Visitor visitor) {
        visitor.visitBooleanSchema(this);
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof BooleanSchema;
    }
}
