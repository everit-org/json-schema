package org.everit.json.schema;

import org.everit.json.schema.listener.CombinedSchemaMismatchEvent;
import org.everit.json.schema.listener.CombinedSchemaValidationEvent;
import org.everit.json.schema.listener.ConditionalSchemaMismatchEvent;
import org.everit.json.schema.listener.ConditionalSchemaValidationEvent;
import org.everit.json.schema.listener.MismatchEvent;
import org.everit.json.schema.listener.SchemaReferencedEvent;

/**
 * Interface to capture which schemas are matching against a specific event in the {@code ValidatingVisitor}.
 */
public interface ValidationListener {

    void mismatch(MismatchEvent event);

    void combinedSchemaMatch(CombinedSchemaValidationEvent event);

    void combinedSchemaMismatch(CombinedSchemaMismatchEvent event);

    void schemaReferenced(SchemaReferencedEvent event);

    void ifSchemaMatch(ConditionalSchemaValidationEvent event);

    void ifSchemaMismatch(ConditionalSchemaMismatchEvent event);

    void thenSchemaMatch(ConditionalSchemaValidationEvent event);

    void thenSchemaMismatch(ConditionalSchemaMismatchEvent event);

    void elseSchemaMatch(ConditionalSchemaValidationEvent event);

    void elseSchemaMismatch(ConditionalSchemaMismatchEvent event);
}

