package org.everit.json.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SchemaVisitorListener {

    private List<Schema> validSchemas = new ArrayList<>();
    private List<Schema> invalidSchemas = new ArrayList<>();

    void addValidSchema(Schema schema) {
        validSchemas.add(schema);
    }

    void addInvalidSchema(Schema schema) {
        invalidSchemas.add(schema);
    }

    List<Schema> getValidSchemas() {
        return validSchemas;
    }

    List<Schema> getInvalidSchemas() {
        return invalidSchemas;
    }

    void clear() {
        validSchemas.clear();
        invalidSchemas.clear();
    }

    @Override
    public String toString() {
        List<String> valid = validSchemas
                .stream().map(s -> String.format("{\"location\": \"%s\", \"schema\": %s}", s.getSchemaLocation(), s))
                .collect(Collectors.toList());

        List<String> invalid = invalidSchemas
                .stream().map(s -> String.format("{\"location\": \"%s\", \"schema\": %s}", s.getSchemaLocation(), s))
                .collect(Collectors.toList());

        return String.format("{\"valid\": %s, \"invalid\": %s}", valid, invalid);
    }
}
