package org.everit.json.schema;

import static org.everit.json.schema.EnumSchema.toJavaValue;

public class ConstSchema extends Schema {

    /**
     * @deprecated This class has been renamed to "Builder".
     */
    @Deprecated
    public static class ConstSchemaBuilder extends Builder {
    }

    public static class Builder extends Schema.Builder<ConstSchema, Builder> {

        private Object permittedValue;

        public Builder permittedValue(Object permittedValue) {
            this.permittedValue = permittedValue;
            return this;
        }

        @Override
        protected Builder getBuilder()
        {
            return this;
        }

        @Override public ConstSchema build() {
            return new ConstSchema(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Object permittedValue;

    protected ConstSchema(Builder builder) {
        super(builder);
        this.permittedValue = toJavaValue(builder.permittedValue);
    }

    @Override void accept(Visitor visitor) {
        visitor.visitConstSchema(this);
    }

    public Object getPermittedValue() {
        return permittedValue;
    }
}
