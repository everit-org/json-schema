package org.everit.json.schema;


import org.everit.json.schema.listener.SubschemaReferencedEvent;
import org.everit.json.schema.listener.SubschemaMatchEvent;
import org.everit.json.schema.listener.SubschemaMismatchEvent;

/**
 * Interface to capture which schemas are matching against a specific event in the {@link ValidatingVisitor}.
 */
public interface SchemaVisitorListener {

    void subschemaMatch(SubschemaMatchEvent matchEvent);

    void subschemaMismatch(SubschemaMismatchEvent mismatchEvent);

    void subschemaReferenced(SubschemaReferencedEvent referencedEvent);

}

