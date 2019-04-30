package org.everit.json.schema;

/**
 * Superclass of all custom types
 */
public abstract class AbstractCustomTypeSchema extends Schema {
    /**
     * Constructor.
     *
     * @param builder
     *         the builder containing the optional title, description and id attributes of the schema
     */
    protected AbstractCustomTypeSchema(Schema.Builder<? extends AbstractCustomTypeSchema> builder) {
	super(builder);
    }

    /**
     * On custom types, it should return an instance of its own visitor implementation
     */
    public abstract Visitor buildVisitor(Object subject,ValidatingVisitor owner);

}