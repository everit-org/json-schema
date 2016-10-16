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
        ls.ifPresent("minLength", Integer.class, builder::minLength);
        ls.ifPresent("maxLength", Integer.class, builder::maxLength);
        ls.ifPresent("pattern", String.class, builder::pattern);
        ls.ifPresent("format", String.class, format -> addFormatValidator(builder, format));
        return builder;
    }

    private void addFormatValidator(final StringSchema.Builder builder, final String formatName) {
        ls.getFormatValidator(formatName).ifPresent(builder::formatValidator);
    }

}
