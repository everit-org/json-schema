package org.everit.json.schema.loader;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;

import java.util.Map;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.loader.internal.DefaultProviderValidators;

/**
 * @author erosb
 */
public class StringSchemaLoader {

    private LoadingState ls;

    //private Map<String, FormatValidator> formatValidators;
    private ProviderValidators providerValidators;

    /**
     * Creates an instance with {@link SpecificationVersion#defaultFormatValidators()}  draft v4 format validators}.
     *
     * @deprecated explicitly specify the format validators with {@link #StringSchemaLoader(LoadingState, ProviderValidators)} instead
     */
    @Deprecated
    public StringSchemaLoader(LoadingState ls) {
        this(ls, new DefaultProviderValidators(DRAFT_4.defaultFormatValidators()));
    }

    StringSchemaLoader(LoadingState ls, ProviderValidators providerValidators) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.providerValidators = requireNonNull(providerValidators, "providerValidators cannot be null");
    }

    public StringSchema.Builder load() {
        StringSchema.Builder builder = StringSchema.builder();
        ls.schemaJson().maybe("minLength").map(JsonValue::requireInteger).ifPresent(builder::minLength);
        ls.schemaJson().maybe("maxLength").map(JsonValue::requireInteger).ifPresent(builder::maxLength);
        ls.schemaJson().maybe("pattern").map(JsonValue::requireString)
                .map(ls.config.regexpFactory::createHandler)
                .ifPresent(builder::pattern);
        ls.schemaJson().maybe("format").map(JsonValue::requireString)
                .ifPresent(format -> addFormatValidator(builder, format));
        return builder;
    }

    private void addFormatValidator(StringSchema.Builder builder, String formatName) {
        FormatValidator formatValidator = providerValidators.getFormatValidator(formatName);
        if (formatValidator != null) {
            builder.formatValidator(formatValidator);
        }
    }

}
