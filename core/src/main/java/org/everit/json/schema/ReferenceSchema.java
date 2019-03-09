package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.everit.json.schema.internal.JSONPrinter;

/**
 * This class is used by {@link org.everit.json.schema.loader.SchemaLoader} to resolve JSON pointers
 * during the construction of the schema. This class has been made mutable to permit the loading of
 * recursive schemas.
 */
public class ReferenceSchema extends Schema {

    /**
     * Builder class for {@link ReferenceSchema}.
     */
    public static class Builder extends Schema.Builder<ReferenceSchema> {

        private ReferenceSchema retval;

        /**
         * The value of {@code "$ref"}
         */
        private String refValue = "";

        /**
         * This method caches its result, so multiple invocations will return referentially the same
         * {@link ReferenceSchema} instance.
         */
        @Override
        public ReferenceSchema build() {
            if (retval == null) {
                retval = new ReferenceSchema(this);
            }
            return retval;
        }

        public Builder refValue(String refValue) {
            this.refValue = refValue;
            return this;
        }

        @Override public ReferenceSchema.Builder unprocessedProperties(Map<String, Object> unprocessedProperties) {
            if (retval != null) {
                retval.unprocessedProperties = new HashMap<>(unprocessedProperties);
            }
            super.unprocessedProperties(unprocessedProperties);
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private Schema referredSchema;

    private final String refValue;

    private Map<String, Object> unprocessedProperties;

    public ReferenceSchema(final Builder builder) {
        super(builder);
        this.refValue = requireNonNull(builder.refValue, "refValue cannot be null");
        this.unprocessedProperties = builder.unprocessedProperties;
    }

    @Override
    public boolean definesProperty(String field) {
        if (referredSchema == null) {
            throw new IllegalStateException("referredSchema must be injected before validation");
        }
        return referredSchema.definesProperty(field);
    }

    public Schema getReferredSchema() {
        return referredSchema;
    }

    /**
     * Called by {@link org.everit.json.schema.loader.SchemaLoader#load()} to set the referred root
     * schema after completing the loading process of the entire schema document.
     *
     * @param referredSchema
     *         the referred schema
     */
    public void setReferredSchema(final Schema referredSchema) {
        if (this.referredSchema != null) {
            throw new IllegalStateException("referredSchema can be injected only once");
        }
        this.referredSchema = referredSchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof ReferenceSchema) {
            ReferenceSchema that = (ReferenceSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(refValue, that.refValue) &&
                    Objects.equals(unprocessedProperties, that.unprocessedProperties) &&
                    Objects.equals(referredSchema, that.referredSchema) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), referredSchema, refValue, unprocessedProperties);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ReferenceSchema;
    }

    @Override void accept(Visitor visitor) {
        visitor.visitReferenceSchema(this);
    }

    @Override void describePropertiesTo(JSONPrinter writer) {
        writer.key("$ref");
        writer.value(refValue);
    }

    @Override public Map<String, Object> getUnprocessedProperties() {
        return unprocessedProperties;
    }
}
