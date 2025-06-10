package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class is used by {@link org.everit.json.schema.loader.SchemaLoader} to resolve JSON pointers
 * during the construction of the schema. This class has been made mutable to permit the loading of
 * recursive schemas.
 */
public class ReferenceSchema extends Schema {

    /**
     * Builder class for {@link ReferenceSchema}.
     */
    public static class Builder extends Schema.Builder<ReferenceSchema, Builder> {

        private ReferenceSchema retval;

        /**
         * The value of {@code "$ref"}
         */
        private String refValue = "";

        @Override
        protected Builder getBuilder()
        {
            return this;
        }

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

        @Override public ReferenceSchema.Builder title(String title) {
            if (retval != null) {
                retval.title = title;
            }
            super.title(title);
            return this;
        }

        @Override public ReferenceSchema.Builder description(String description) {
            if (retval != null) {
                retval.description = description;
            }
            super.description(description);
            return this;
        }

        @Override public ReferenceSchema.Builder schemaLocation(SchemaLocation location) {
            if (retval != null) {
                retval.schemaLocation = location;
            }
            super.schemaLocation(location);
            return this;
        }

        public Builder copy() {
            Builder copy = new Builder();
            if (this.retval != null) {
                copy.build().setReferredSchema(this.retval.getReferredSchema());
            }
            return copy;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private Schema referredSchema;

    private final String refValue;

    private Map<String, Object> unprocessedProperties;

    private String title;

    private String description;

    private SchemaLocation schemaLocation;

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

    public String getReferenceValue() {
        return refValue;
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
        if (this == o) {
            return true;
        }
        if (o instanceof ReferenceSchema) {
            ReferenceSchema that = (ReferenceSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(refValue, that.refValue) &&
                    Objects.equals(unprocessedProperties, that.unprocessedProperties) &&
                    Objects.equals(title, that.title) &&
                    Objects.equals(description, that.description) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), refValue, unprocessedProperties, title, description);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ReferenceSchema;
    }

    @Override void accept(Visitor visitor) {
        visitor.visitReferenceSchema(this);
    }

    @Override public Map<String, Object> getUnprocessedProperties() {
        return unprocessedProperties == null ? super.getUnprocessedProperties() : unprocessedProperties;
    }

    @Override public String getTitle() {
        return title == null ? super.getTitle() : title;
    }

    @Override public String getDescription() {
        return description == null ? super.getDescription() : description;
    }

    @Override public SchemaLocation getLocation() {
        return schemaLocation == null ? super.getLocation() : schemaLocation;
    }
}
