package org.everit.json.schema;

import org.json.JSONObject;

import java.util.Objects;

import static org.everit.json.schema.EnumSchema.toJavaValue;

public class ConstSchema extends Schema {

    public static class ConstSchemaBuilder extends Schema.Builder<ConstSchema> {

        private Object permittedValue;

        public ConstSchemaBuilder permittedValue(Object permittedValue) {
            this.permittedValue = permittedValue;
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

    @Override public void validate(Object subject) {
        if (isNull(subject) && isNull(permittedValue)) {
            return;
        }
        Object effectiveSubject = toJavaValue(subject);
        if (!ObjectComparator.deepEquals(effectiveSubject , this.permittedValue)) {
            throw failure("", "const");
        }
    }

    private boolean isNull(Object obj) {
        return obj == null || JSONObject.NULL.equals(obj);
    }
}
