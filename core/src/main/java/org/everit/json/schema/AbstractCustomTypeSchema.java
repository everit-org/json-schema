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
     * 
     * @param subject
     * 		the subject/context of the new visitor
     * 
     * @param owner
     * 		the owner of the new visitor
     * 
     * @return
     * 		The newly created Visitor for this custom type
     * 
     */
    public abstract Visitor buildVisitor(Object subject,ValidatingVisitor owner);

    @Override void accept(Visitor visitor) {
        visitor.visitCustomTypeSchema(this);
    }

}