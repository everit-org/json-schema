package org.everit.json.schema.event;

/**
 * Interface to capture which schemas are matching against a specific event in the {@code ValidatingVisitor}.
 */
public interface ValidationListener {

    ValidationListener NOOP = new ValidationListener() {
    };

    default void mismatch(MismatchEvent event) {
    }

    default void combinedSchemaMatch(CombinedSchemaMatchEvent event) {
    }

    default void combinedSchemaMismatch(CombinedSchemaMismatchEvent event) {
    }

    default void schemaReferenced(SchemaReferencedEvent event) {
    }

    default void ifSchemaMatch(ConditionalSchemaMatchEvent event) {
    }

    default void ifSchemaMismatch(ConditionalSchemaMismatchEvent event) {
    }

    default void thenSchemaMatch(ConditionalSchemaMatchEvent event) {
    }

    default void thenSchemaMismatch(ConditionalSchemaMismatchEvent event) {
    }

    default void elseSchemaMatch(ConditionalSchemaMatchEvent event) {

    }

    default void elseSchemaMismatch(ConditionalSchemaMismatchEvent event) {
    }
}

