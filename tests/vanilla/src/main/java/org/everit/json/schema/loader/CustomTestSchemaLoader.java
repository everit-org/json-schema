package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.util.List;

import org.everit.json.schema.CustomTestSchema;
import org.everit.json.schema.Schema;

/**
 * @author jmfernandez
 */
public class CustomTestSchemaLoader {
    static final List<String> SCHEMA_PROPS = asList("rightValue");

    private final LoadingState ls;

    private final LoaderConfig config;

    private final SchemaLoader defaultLoader;

    public CustomTestSchemaLoader(LoadingState ls, LoaderConfig config, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.config = requireNonNull(config, "config cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    CustomTestSchema.Builder load() {
        CustomTestSchema.Builder builder = CustomTestSchema.builder();
        ls.schemaJson().maybe("rightValue").map(JsonValue::requireString).ifPresent(builder::rightValue);
        return builder;
    }

    // This method is 
    public static CustomTestSchema.Builder schemaBuilderLoader(LoadingState ls, LoaderConfig config, SchemaLoader schemaLoader) {
        return new CustomTestSchemaLoader(ls, config, schemaLoader).load();
    }
    
    public static final List<String> schemaKeywords() {
        return CustomTestSchemaLoader.SCHEMA_PROPS;
    }
}
