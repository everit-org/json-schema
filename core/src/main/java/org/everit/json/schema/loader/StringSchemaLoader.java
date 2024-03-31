package org.everit.json.schema.loader;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;

import java.util.Map;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.StringSchema;

public class StringSchemaLoader {

    private LoadingState ls;

    private FormatValidatorLoader formatValidatorLoader;

    @Deprecated
    public StringSchemaLoader(LoadingState ls) {
        this(ls, new FormatValidatorLoader(DRAFT_4.defaultFormatValidators()));
    }

    StringSchemaLoader(LoadingState ls, FormatValidatorLoader formatValidatorLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.formatValidatorLoader = requireNonNull(formatValidatorLoader, "formatValidatorLoader cannot be null");
    }

    public StringSchema.Builder load() {
        StringSchema.Builder builder = StringSchema.builder();
        ls.schemaJson().maybe("minLength").map(JsonValue::requireInteger).ifPresent(builder::minLength);
        ls.schemaJson().maybe("maxLength").map(JsonValue::requireInteger).ifPresent(builder::maxLength);
        ls.schemaJson().maybe("pattern").map(JsonValue::requireString)
                .map(ls.config.regexpFactory::createHandler)
                .ifPresent(builder::pattern);
        ls.schemaJson().maybe("format").map(JsonValue::requireString)
                .ifPresent(format -> {
                    FormatValidator validator = formatValidatorLoader.loadValidator(format);
                    if (validator != null) {
                        builder.formatValidator(validator);
                    } else {
                        // Handling  the unsupported format with proper message.
                        System.out.println("Unsupported format: " + format);
                    }
                });
        return builder;
    }
}

