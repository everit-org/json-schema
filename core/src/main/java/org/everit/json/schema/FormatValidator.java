package org.everit.json.schema;

import com.google.common.base.Optional;

/**
 * Created by fpeter on 2017. 01. 16..
 */
public interface FormatValidator {

    /**
     * No-operation implementation (never throws {always returns {@link Optional#absent()}).
     */
    FormatValidator NONE = new AbstractFormatValidator() {
        @Override
        public Optional<String> validate(String subject) {
            return Optional.absent();
        }
    };

    /**
     * Implementation-specific validation of {@code subject}. If a validation error occurs then
     * implementations should return a programmer-friendly error message as a String wrapped in an
     * Optional. If the validation succeeded then {@link Optional#absent() an empty optional} should be
     * returned.
     *
     * @param subject the string to be validated
     * @return an {@code Optional} wrapping the error message if a validation error occured, otherwise
     * {@link Optional#absent() an empty optional}.
     */
    Optional<String> validate(String subject);

    /**
     * Provides the name of this format.
     * <p>
     * Unless specified otherwise the {@link org.everit.json.schema.loader.SchemaLoader} will use this
     * name to recognize string schemas using this format.
     * <p>
     * The default implementation of this method returns {@code "unnamed-format"}. It is strongly
     * recommended for implementations to give a more meaningful name by overriding this method.
     *
     * @return the format name.
     */
    String formatName();
}
