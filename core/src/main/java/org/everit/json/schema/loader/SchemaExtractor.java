package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.Schema;

interface SchemaExtractor {

    Collection<Schema.Builder<?>> extract(LoadingState ls);

}

class EnumSchemaExtractor implements SchemaExtractor {

    @Override public Collection<Schema.Builder<?>> extract(LoadingState ls) {
        if (!ls.schemaJson().containsKey("enum")) {
            return emptyList();
        }
        EnumSchema.Builder builder = EnumSchema.builder();
        Set<Object> possibleValues = new HashSet<>();
        ls.schemaJson().require("enum").requireArray().forEach((i, item) -> possibleValues.add(item.unwrap()));
        builder.possibleValues(possibleValues);
        return asList(builder);
    }

}
