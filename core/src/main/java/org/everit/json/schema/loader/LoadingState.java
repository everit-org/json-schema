package org.everit.json.schema.loader;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.loader.internal.TypeBasedMultiplexer;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class LoadingState {

    final SchemaClient httpClient;

    final Map<String, FormatValidator> formatValidators;

    URI id = null;

    final Map<String, ReferenceSchema.Builder> pointerSchemas;

    final JSONObject rootSchemaJson;

    final JSONObject schemaJson;

    LoadingState(SchemaClient httpClient,
            Map<String, FormatValidator> formatValidators,
            Map<String, ReferenceSchema.Builder> pointerSchemas,
            JSONObject rootSchemaJson,
            JSONObject schemaJson,
            URI id) {
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
        this.formatValidators = requireNonNull(formatValidators, "formatValidators cannot be null");
        this.pointerSchemas = requireNonNull(pointerSchemas, "pointerSchemas cannot be null");
        this.rootSchemaJson = requireNonNull(rootSchemaJson, "rootSchemaJson cannot be null");
        this.schemaJson = requireNonNull(schemaJson, "schemaJson cannot be null");
        this.id = id;
    }

    <E> void ifPresent(final String key, final Class<E> expectedType,
            final Consumer<E> consumer) {
        if (schemaJson.has(key)) {
            @SuppressWarnings("unchecked")
            E value = (E) schemaJson.get(key);
            try {
                consumer.accept(value);
            } catch (ClassCastException e) {
                throw new SchemaException(key, expectedType, value);
            }
        }
    }

    TypeBasedMultiplexer typeMultiplexer(Object obj) {
        return typeMultiplexer(null, obj);
    }

    TypeBasedMultiplexer typeMultiplexer(String keyOfObj, Object obj) {
        TypeBasedMultiplexer multiplexer = new TypeBasedMultiplexer(keyOfObj, obj, id);
        multiplexer.addResolutionScopeChangeListener(scope -> {
            this.id = scope;
        });
        return multiplexer;
    }



}
