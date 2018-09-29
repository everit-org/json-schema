package org.everit.json.schema;

import java.io.StringWriter;
import java.util.Objects;

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONWriter;

/**
 * Superclass of all other schema validator classes of this package.
 */
public abstract class Schema {

    /**
     * Abstract builder class for the builder classes of {@code Schema} subclasses. This builder is
     * used to load the generic properties of all types of schemas like {@code title} or
     * {@code description}.
     *
     * @param <S>
     *         the type of the schema being built by the builder subclass.
     */
    public abstract static class Builder<S extends Schema> {

        private String title;

        private String description;

        private String id;

        private String schemaLocation;

        private Object defaultValue;

        private Boolean nullable = null;

        private Boolean readOnly = null;

        private Boolean writeOnly = null;

        public Builder<S> title(String title) {
            this.title = title;
            return this;
        }

        public Builder<S> description(String description) {
            this.description = description;
            return this;
        }

        public Builder<S> id(String id) {
            this.id = id;
            return this;
        }

        public Builder<S> schemaLocation(String schemaLocation) {
            this.schemaLocation = schemaLocation;
            return this;
        }

        public Builder<S> defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<S> nullable(Boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public Builder<S> readOnly(Boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public Builder<S> writeOnly(Boolean writeOnly) {
            this.writeOnly = writeOnly;
            return this;
        }

        public abstract S build();

    }

    private final String title;

    private final String description;

    private final String id;

    protected final String schemaLocation;

    private final Object defaultValue;

    private final Boolean nullable;

    private final Boolean readOnly;

    private final Boolean writeOnly;

    /**
     * Constructor.
     *
     * @param builder
     *         the builder containing the optional title, description and id attributes of the schema
     */
    protected Schema(Builder<?> builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.id = builder.id;
        this.schemaLocation = builder.schemaLocation;
        this.defaultValue = builder.defaultValue;
        this.nullable = builder.nullable;
        this.readOnly = builder.readOnly;
        this.writeOnly = builder.writeOnly;
    }

    /**
     * Performs the schema validation.
     *
     * @param subject
     *         the object to be validated
     * @throws ValidationException
     *         if the {@code subject} is invalid against this schema.
     */
    public void validate(Object subject) {
        Validator.builder().build().performValidation(this, subject);
    }

    /**
     * Determines if this {@code Schema} instance defines any restrictions for the object property
     * denoted by {@code field}. The {@code field} should be a JSON pointer, denoting the property to
     * be queried.
     * <p>
     * For example the field {@code "#/rectangle/a"} is defined by the following schema:
     * </p>
     * <pre>
     * <code>
     * objectWithSchemaRectangleDep" : {
     *   "type" : "object",
     *   "dependencies" : {
     *       "d" : {
     *           "type" : "object",
     *           "properties" : {
     *               "rectangle" : {"$ref" : "#/definitions/Rectangle" }
     *           }
     *       }
     *   },
     *   "definitions" : {
     *       "size" : {
     *           "type" : "number",
     *           "minimum" : 0
     *       },
     *       "Rectangle" : {
     *           "type" : "object",
     *           "properties" : {
     *               "a" : {"$ref" : "#/definitions/size"},
     *               "b" : {"$ref" : "#/definitions/size"}
     *           }
     *       }
     *    }
     * }
     * </code>
     * </pre>
     * The default implementation of this method always returns false.
     *
     * @param field
     *         should be a JSON pointer in its string representation.
     * @return {@code true} if the propertty denoted by {@code field} is defined by this schema
     * instance
     */
    public boolean definesProperty(String field) {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Schema) {
            Schema schema = (Schema) o;
            return schema.canEqual(this) &&
                    Objects.equals(title, schema.title) &&
                    Objects.equals(defaultValue, schema.defaultValue) &&
                    Objects.equals(description, schema.description) &&
                    Objects.equals(id, schema.id) &&
                    Objects.equals(nullable, schema.nullable) &&
                    Objects.equals(readOnly, schema.readOnly) &&
                    Objects.equals(writeOnly, schema.writeOnly);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, id, defaultValue, nullable, readOnly, writeOnly);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    public Boolean isNullable() {
        return nullable;
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public Boolean isWriteOnly() {
        return writeOnly;
    }

    /**
     * Describes the instance as a JSONObject to {@code writer}.
     * <p>
     * First it adds the {@code "title} , {@code "description"} and {@code "id"} properties then calls
     * {@link #describePropertiesTo(JSONPrinter)}, which will add the subclass-specific properties.
     * <p>
     * It is used by {@link #toString()} to serialize the schema instance into its JSON representation.
     *
     * @param writer
     *         it will receive the schema description
     */
    public void describeTo(JSONPrinter writer) {
        writer.object();
        writer.ifPresent("title", title);
        writer.ifPresent("description", description);
        writer.ifPresent("id", id);
        writer.ifPresent("default", defaultValue);
        writer.ifPresent("nullable", nullable);
        writer.ifPresent("readOnly", readOnly);
        writer.ifPresent("writeOnly", writeOnly);
        describePropertiesTo(writer);
        writer.endObject();
    }

    /**
     * Subclasses are supposed to override this method to describe the subclass-specific attributes.
     * This method is called by {@link #describeTo(JSONPrinter)} after adding the generic properties if
     * they are present ({@code id}, {@code title} and {@code description}). As a side effect,
     * overriding subclasses don't have to open and close the object with {@link JSONWriter#object()}
     * and {@link JSONWriter#endObject()}.
     *
     * @param writer
     *         it will receive the schema description
     */
    void describePropertiesTo(JSONPrinter writer) {

    }

    abstract void accept(Visitor visitor);

    @Override
    public String toString() {
        StringWriter w = new StringWriter();
        describeTo(new JSONPrinter(w));
        return w.getBuffer().toString();
    }

    @Deprecated
    protected ValidationException failure(String message, String keyword) {
        return new ValidationException(this, message, keyword, schemaLocation);
    }

    @Deprecated
    protected ValidationException failure(Class<?> expectedType, Object actualValue) {
        return new ValidationException(this, expectedType, actualValue, "type", schemaLocation);
    }

    /**
     * Since we add state in subclasses, but want those subclasses to be non final, this allows us to
     * have equals methods that satisfy the equals contract.
     * <p>
     * http://www.artima.com/lejava/articles/equality.html
     *
     * @param other
     *         the subject of comparison
     * @return {@code true } if {@code this} can be equal to {@code other}
     */
    protected boolean canEqual(Object other) {
        return (other instanceof Schema);
    }
}
