package org.everit.json.schema.loader;

import org.everit.json.schema.ConditionalSchema;
import org.everit.json.schema.Schema;

import static java.util.Objects.requireNonNull;

public class ConditionalSchemaLoader {

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public ConditionalSchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    public Schema.Builder<?> load() {
        ConditionalSchema.Builder builder = ConditionalSchema.builder();
        if (ls.schemaJson().containsKey("if")) {
            builder.ifSchema(defaultLoader.loadChild(ls.schemaJson().require("if").requireObject()).build());
        }
        if (ls.schemaJson().containsKey("then")) {
            builder.thenSchema(defaultLoader.loadChild(ls.schemaJson().require("then").requireObject()).build());
        }
        if (ls.schemaJson().containsKey("else")) {
           builder.elseSchema(defaultLoader.loadChild(ls.schemaJson().require("else").requireObject()).build());
        }
        return builder;
    }
}
