package org.everit.json.schema.event;

/**
 * Interface to capture which schemas are matching against a specific event in the {@code ValidatingVisitor}.
 * <p>
 * All methods of this interface have a default empty implementation, so if an interface implementation is
 * interested only about a few specific event types, there is no need to add empty overrides of the unhandles events.
 * </p>
 */
public interface ValidationListener {

    /**
     * Default no-operation implementation
     */
    ValidationListener NOOP = new ValidationListener() {
    };

    /**
     * Called when a {@link org.everit.json.schema.CombinedSchema}'s given subschema matches the instance.
     * <p>
     * The {@link org.everit.json.schema.CombinedSchema} (which means an {@code "allOf"} or {@code "anyOf"} or
     * {@code "oneOf"} schema) can be referenced by calling {@link CombinedSchemaMatchEvent#getSchema()} and the
     * matching subschema is returned by {@link CombinedSchemaMatchEvent#getSubSchema()}.
     * </p>
     */
    default void combinedSchemaMatch(CombinedSchemaMatchEvent event) {
    }

    /**
     * Called when a {@link org.everit.json.schema.CombinedSchema}'s given subschema does not match the instance.
     * <p>
     * The {@link org.everit.json.schema.CombinedSchema} (which means an {@code "allOf"} or {@code "anyOf"} or
     * {@code "oneOf"} schema) can be referenced by calling {@link CombinedSchemaMismatchEvent#getSchema()} and the
     * matching subschema is returned by {@link CombinedSchemaMismatchEvent#getSubSchema()}. The validation failure
     * of the subschema is returned by {@link CombinedSchemaMismatchEvent#getFailure()}.
     * </p>
     *
     * <p>
     * <em>Note: the invocation of this method by the validator does not necessarily mean that the validation
     * against the {@link org.everit.json.schema.CombinedSchema} will fail. In the case of the {@code "anyOf"} and
     * {@code "oneOf"} schemas it can be an intermediate failure while the root-level schema validation still
     * passes.</em>
     * </p>
     */

    default void combinedSchemaMismatch(CombinedSchemaMismatchEvent event) {
    }

    /**
     * Called when a {@code "$ref"} JSON reference is resolved.
     * <p>
     * The referred schema is returned by {@link SchemaReferencedEvent#getReferredSchema()}, and the
     * {@link org.everit.json.schema.ReferenceSchema "$ref"} itself is returned by
     * {@link SchemaReferencedEvent#getSchema()}
     * </p>
     */
    default void schemaReferenced(SchemaReferencedEvent event) {
    }

    /**
     * Called when an {@code "if"} schema matches.
     * <p>
     * The {@link org.everit.json.schema.ConditionalSchema} (holding both the {@code "if"}, {@code "then"} and
     * {@code "else"} schemas) is returned by {@link ConditionalSchemaMatchEvent#getSchema()}, and
     * {@link ConditionalSchemaMatchEvent#getKeyword()} always returns
     * {@link org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword#IF} in this method.
     * </p>
     *
     * <p>
     * <em>Note: the invocation of this method does not necessarily mean that the validation of the
     * {@link org.everit.json.schema.ConditionalSchema} will succeed. Instead it means that the evaluation continues
     * with the {@code "then"} schema.</em>
     * </p>
     */
    default void ifSchemaMatch(ConditionalSchemaMatchEvent event) {
    }

    /**
     * Called when the instance does not pass the validation against an {@code "if"} schema.
     * <p>
     * The {@link org.everit.json.schema.ConditionalSchema} (holding both the {@code "if"}, {@code "then"} and
     * {@code "else"} schemas) is returned by {@link ConditionalSchemaMatchEvent#getSchema()}, and
     * {@link ConditionalSchemaMatchEvent#getKeyword()} always returns
     * {@link org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword#IF} in this method.
     * </p>
     *
     * <p>
     * <em>Note: the invocation of this method does not necessarily mean that the validation of the
     * {@link org.everit.json.schema.ConditionalSchema} will fail. Instead it means that the evaluation continues
     * with the {@code "else"} schema.</em>
     * </p>
     */
    default void ifSchemaMismatch(ConditionalSchemaMismatchEvent event) {
    }

    /**
     * Called when the instance passes the validation against a {@code "then"} schema.
     * <p>
     * The {@link org.everit.json.schema.ConditionalSchema} (holding both the {@code "if"}, {@code "then"} and
     * {@code "else"} schemas) is returned by {@link ConditionalSchemaMatchEvent#getSchema()}, and
     * {@link ConditionalSchemaMatchEvent#getKeyword()} always returns
     * {@link org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword#THEN} in this method.
     * </p>
     */
    default void thenSchemaMatch(ConditionalSchemaMatchEvent event) {
    }

    /**
     * Called when the instance does not pass the validation against a {@code "then"} schema.
     * <p>
     * The {@link org.everit.json.schema.ConditionalSchema} (holding both the {@code "if"}, {@code "then"} and
     * {@code "else"} schemas) is returned by {@link ConditionalSchemaMatchEvent#getSchema()}, and
     * {@link ConditionalSchemaMatchEvent#getKeyword()} always returns
     * {@link org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword#THEN} in this method.
     * </p>
     */
    default void thenSchemaMismatch(ConditionalSchemaMismatchEvent event) {
    }

    /**
     * Called when the instance passes the validation against a {@code "else"} schema.
     * <p>
     * The {@link org.everit.json.schema.ConditionalSchema} (holding both the {@code "if"}, {@code "then"} and
     * {@code "else"} schemas) is returned by {@link ConditionalSchemaMatchEvent#getSchema()}, and
     * {@link ConditionalSchemaMatchEvent#getKeyword()} always returns
     * {@link org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword#ELSE} in this method.
     * </p>
     */
    default void elseSchemaMatch(ConditionalSchemaMatchEvent event) {

    }

    /**
     * Called when the instance does not pass the validation against a {@code "else"} schema.
     * <p>
     * The {@link org.everit.json.schema.ConditionalSchema} (holding both the {@code "if"}, {@code "then"} and
     * {@code "else"} schemas) is returned by {@link ConditionalSchemaMatchEvent#getSchema()}, and
     * {@link ConditionalSchemaMatchEvent#getKeyword()} always returns
     * {@link org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword#ELSE} in this method.
     * </p>
     */
    default void elseSchemaMismatch(ConditionalSchemaMismatchEvent event) {
    }
}

