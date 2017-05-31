package org.everit.json.schema.loader;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.StringSchema;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
public class StringSchemaLoader {

    private LoadingState ls;

    private Map<String, FormatValidator> formatValidators;

    /**
     * Creates an instance with {@link FormatValidator#v4Defaults() draft v4 format validators}.
     *
     * @deprecated explicitly specify the format validators with {@link #StringSchemaLoader(LoadingState, Map)} instead
     */
    @Deprecated
    public StringSchemaLoader(LoadingState ls) {
        this(ls, FormatValidator.v4Defaults());
    }

    StringSchemaLoader(LoadingState ls, Map<String, FormatValidator> formatValidators) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.formatValidators = unmodifiableMap(requireNonNull(formatValidators, "formatValidators cannot be null"));
    }

    public StringSchema.Builder load() {
        StringSchema.Builder builder = StringSchema.builder();
        ls.schemaJson().maybe("minLength").map(JsonValue::requireInteger).ifPresent(builder::minLength);
        ls.schemaJson().maybe("maxLength").map(JsonValue::requireInteger).ifPresent(builder::maxLength);
        ls.schemaJson().maybe("pattern").map(JsonValue::requireString).ifPresent(builder::pattern);
        ls.schemaJson().maybe("format").map(JsonValue::requireString)
                .ifPresent(format -> addFormatValidator(builder, format));
        return builder;
    }

    private void addFormatValidator(StringSchema.Builder builder, String formatName) {
        FormatValidator formatValidator = formatValidators.get(formatName);
        if (formatValidator != null) {
            builder.formatValidator(formatValidator);
        }
    }

}
