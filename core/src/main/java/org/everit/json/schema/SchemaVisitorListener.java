package org.everit.json.schema;

/**
 * Interface to capture which schemas are matching against a specific event in the {@link ValidatingVisitor}.
 */
public interface SchemaVisitorListener {

    void addValidSchema(Schema schema);

    void addInvalidSchema(Schema schema);

}

