package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.ExtractionResult.EMPTY;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.Schema;

class ExtractionResult {

    static final ExtractionResult EMPTY = new ExtractionResult(emptySet(), emptySet());

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

class EnumSchemaExtractor implements SchemaExtractor {

    @Override public ExtractionResult extract(JsonObject schemaJson) {
        if (!schemaJson.containsKey("enum")) {
            return EMPTY;
        }
        EnumSchema.Builder builder = EnumSchema.builder();
        Set<Object> possibleValues = new HashSet<>();
        schemaJson.require("enum").requireArray().forEach((i, item) -> possibleValues.add(item.unwrap()));
        builder.possibleValues(possibleValues);
        return new ExtractionResult("enum", asList(builder));
    }

}
