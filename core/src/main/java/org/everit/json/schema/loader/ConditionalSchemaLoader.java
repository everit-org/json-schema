package org.everit.json.schema.loader;

import org.everit.json.schema.ConditionalSchema;
import org.everit.json.schema.Schema;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ConditionalSchemaLoader {

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public ConditionalSchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    public Schema.Builder<?> load() {
        Schema ifSchema = null;
        Schema thenSchema = null;
        Schema elseSchema = null;
        if (ls.schemaJson().containsKey("if")) {
            ifSchema = defaultLoader.loadChild(ls.schemaJson().require("if").requireObject()).build();
        }
        if (ls.schemaJson().containsKey("then")) {
            thenSchema = defaultLoader.loadChild(ls.schemaJson().require("then").requireObject()).build();
        }
        if (ls.schemaJson().containsKey("else")) {
            elseSchema = defaultLoader.loadChild(ls.schemaJson().require("else").requireObject()).build();
        }
        return ConditionalSchema.builder().ifSchema(ifSchema).thenSchema(thenSchema).elseSchema(elseSchema);
    }
}
