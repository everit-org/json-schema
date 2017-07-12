package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;

/**
 * Boolean schema validator.
 */
public class BooleanSchema extends Schema {

    /**
     * Builder class for {@link BooleanSchema}.
     */
    public static class Builder extends Schema.Builder<BooleanSchema> {

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
    public void validate(final Object subject) {
        if (!(subject instanceof Boolean)) {
            throw failure(Boolean.class, subject);
        }
    }

    @Override
    void describePropertiesTo(final JSONPrinter writer) {
        writer.key("type");
        writer.value("boolean");
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

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof BooleanSchema;
    }
}
