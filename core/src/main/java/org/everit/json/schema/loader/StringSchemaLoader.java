package org.everit.json.schema.loader;

import com.google.common.base.Optional;
import org.everit.json.schema.Consumer;
import org.everit.json.schema.FormatValidator;
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
        final StringSchema.Builder builder = StringSchema.builder();
        ls.ifPresent("minLength", Integer.class, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                builder.minLength(integer);
            }
        });
        ls.ifPresent("maxLength", Integer.class, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                builder.maxLength(integer);
            }
        });
        ls.ifPresent("pattern", String.class, new Consumer<String>() {
            @Override
            public void accept(String s) {
                builder.pattern(s);
            }
        });
        ls.ifPresent("format", String.class, new Consumer<String>() {
            @Override
            public void accept(String s) {
                Optional<FormatValidator> validator = ls.getFormatValidator(s);
                if (validator.isPresent()) {
                    builder.formatValidator(validator.get());
                }
            }
        });
        return builder;
    }

}
