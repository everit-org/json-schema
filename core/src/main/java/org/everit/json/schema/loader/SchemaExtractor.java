package org.everit.json.schema.loader;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.Schema;

class ExtractionResult {

    final Set<String> consumedKeys;

    final Collection<Schema.Builder<?>> extractedSchemas;

    ExtractionResult(Set<String> consumedKeys, Collection<Schema.Builder<?>> extractedSchemas) {
        this.consumedKeys = requireNonNull(consumedKeys, "consumedKeys cannot be null");
        this.extractedSchemas = requireNonNull(extractedSchemas, "extractedSchemas cannot be null");
    }

    ExtractionResult(String consumedKeys, Collection<Schema.Builder<?>> extactedSchemas) {
        this(singleton(consumedKeys), extactedSchemas);
    }

}

interface SchemaExtractor {

    ExtractionResult extract(JsonObject schemaJson);

}

abstract class AbstractSchemaExtractor implements SchemaExtractor {

    protected JsonObject schemaJson;

    private Set<String> consumedKeys;

    @Override
    public final ExtractionResult extract(JsonObject schemaJson) {
        this.schemaJson = requireNonNull(schemaJson, "schemaJson cannot be null");
        consumedKeys = new HashSet<>(schemaJson.keySet().size());
        return new ExtractionResult(consumedKeys, extract());
    }

    private void keyConsumed(String key) {
        if (schemaJson.keySet().contains(key)) {
            consumedKeys.add(key);
        }
    }

    protected JsonValue require(String key) {
        keyConsumed(key);
        return schemaJson.require(key);
    }

    protected Optional<JsonValue> maybe(String key) {
        keyConsumed(key);
        return schemaJson.maybe(key);
    }

    protected boolean containsKey(String key) {
        return schemaJson.containsKey(key);
    }

    protected abstract List<Schema.Builder<?>> extract();
}

class EnumSchemaExtractor extends AbstractSchemaExtractor {

    @Override protected List<Schema.Builder<?>> extract() {
        if (!containsKey("enum")) {
            return emptyList();
        }
        EnumSchema.Builder builder = EnumSchema.builder();
        Set<Object> possibleValues = new HashSet<>();
        require("enum").requireArray().forEach((i, item) -> possibleValues.add(item.unwrap()));
        builder.possibleValues(possibleValues);
        return singletonList(builder);
    }

}

class ReferenceSchemaExtractor extends AbstractSchemaExtractor {

    @Override protected List<Schema.Builder<?>> extract() {
        if (containsKey("$ref")) {
            String ref = require("$ref").requireString();
            return singletonList(new ReferenceLookup(schemaJson.ls).lookup(ref, schemaJson));
        }
        return emptyList();
    }
}
