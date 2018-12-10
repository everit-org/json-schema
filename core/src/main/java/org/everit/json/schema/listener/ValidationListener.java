package org.everit.json.schema.listener;

/**
 * Interface to capture which schemas are matching against a specific event in the {@code ValidatingVisitor}.
 */
public interface ValidationListener {

    ValidationListener NOOP = new ValidationListener() {
        @Override public void mismatch(MismatchEvent event) {
        }

        @Override public void combinedSchemaMatch(CombinedSchemaMatchEvent event) {
        }

        @Override public void combinedSchemaMismatch(CombinedSchemaMismatchEvent event) {
        }

        @Override public void schemaReferenced(SchemaReferencedEvent event) {
        }

        @Override public void ifSchemaMatch(ConditionalSchemaMatchEvent event) {
        }

        @Override public void ifSchemaMismatch(ConditionalSchemaMismatchEvent event) {
        }

        @Override public void thenSchemaMatch(ConditionalSchemaMatchEvent event) {
        }

        @Override public void thenSchemaMismatch(ConditionalSchemaMismatchEvent event) {
        }

        @Override public void elseSchemaMatch(ConditionalSchemaMatchEvent event) {
        }

        @Override public void elseSchemaMismatch(ConditionalSchemaMismatchEvent event) {
        }
    };

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

