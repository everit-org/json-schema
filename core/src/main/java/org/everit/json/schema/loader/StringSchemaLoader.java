package org.everit.json.schema.loader;

import org.everit.json.schema.StringSchema;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
public class StringSchemaLoader {

    private LoadingState ls;

    public StringSchemaLoader(LoadingState ls) {
        this.ls = requireNonNull(ls, "ls cannot be null");
    }

    public StringSchema.Builder load() {
        StringSchema.Builder builder = StringSchema.builder();
        ls.schemaJson.maybe("minLength").map(JsonValue::requireInteger).ifPresent(builder::minLength);
        ls.schemaJson.maybe("maxLength").map(JsonValue::requireInteger).ifPresent(builder::maxLength);
        ls.schemaJson.maybe("pattern").map(JsonValue::requireString).ifPresent(builder::pattern);
        ls.schemaJson.maybe("format").map(JsonValue::requireString)
                .ifPresent(format -> addFormatValidator(builder, format));
        return builder;
    }

    private void addFormatValidator(StringSchema.Builder builder, String formatName) {
        ls.getFormatValidator(formatName).ifPresent(builder::formatValidator);
    }

}
