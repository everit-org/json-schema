package org.everit.json.schema;

import static org.everit.json.schema.EnumSchema.toJavaValue;

public class ConstSchema extends Schema {

    public static class ConstSchemaBuilder extends Schema.Builder<ConstSchema, ConstSchemaBuilder> {

        private Object permittedValue;

        public ConstSchemaBuilder permittedValue(Object permittedValue) {
            this.permittedValue = permittedValue;
            return this;
        }

        @Override
        protected ConstSchemaBuilder getBuilder()
        {
            return this;
        }

        @Override public ConstSchema build() {
            return new ConstSchema(this);
        }
    }

    public static ConstSchemaBuilder builder() {
        return new ConstSchemaBuilder();
    }

    private final Object permittedValue;

    protected ConstSchema(ConstSchemaBuilder builder) {
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
