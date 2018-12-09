package org.everit.json.schema.listener;

/**
 * Interface to capture which schemas are matching against a specific event in the {@code ValidatingVisitor}.
 */
public interface ValidationListener {

    void mismatch(MismatchEvent event);

    void combinedSchemaMatch(CombinedSchemaMatchEvent event);

    void combinedSchemaMismatch(CombinedSchemaMismatchEvent event);

    void schemaReferenced(SchemaReferencedEvent event);

    void ifSchemaMatch(ConditionalSchemaMatchEvent event);

    void ifSchemaMismatch(ConditionalSchemaMismatchEvent event);

    void thenSchemaMatch(ConditionalSchemaMatchEvent event);

    void thenSchemaMismatch(ConditionalSchemaMismatchEvent event);

    void elseSchemaMatch(ConditionalSchemaMatchEvent event);

    void elseSchemaMismatch(ConditionalSchemaMismatchEvent event);
}

