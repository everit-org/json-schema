package org.everit.json.schema;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;

import java.util.List;

import static java.lang.String.format;

/**
 * Thrown by {@link org.everit.json.schema.loader.SchemaLoader#load()} when it encounters
 * un-parseable schema JSON definition.
 *
 * @author @erosb
 */
public class SchemaException extends RuntimeException {

    private static final long serialVersionUID = 5987489689035036987L;

    private static Object typeOfValue(final Object actualValue) {
        return actualValue == null ? "null" : actualValue.getClass().getSimpleName();
    }

    private static String joinClassNames(final List<Class<?>> expectedTypes) {
        return Joiner.on(", ").join(FluentIterable.from(expectedTypes)
                .transform(new Function<Class<?>, String>() {
                    @Override
                    public String apply(Class<?> input) {
                        return input.getSimpleName();
                    }
                }));
    }

    public SchemaException(final String message) {
        super(message);
    }

    public SchemaException(final String key, final Class<?> expectedType, final Object actualValue) {
        this(format("key %s : expected type: %s , found : %s", key, expectedType
                .getSimpleName(), typeOfValue(actualValue)));
    }

    public SchemaException(final String key, final List<Class<?>> expectedTypes,
            final Object actualValue) {
        this(format("key %s: expected type is one of %s, found: %s",
                key, joinClassNames(expectedTypes), typeOfValue(actualValue)));
    }

    public SchemaException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
