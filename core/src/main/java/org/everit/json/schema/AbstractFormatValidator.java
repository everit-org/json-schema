package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;

import org.everit.json.schema.internal.DateTimeFormatValidator;
import org.everit.json.schema.internal.EmailFormatValidator;
import org.everit.json.schema.internal.HostnameFormatValidator;
import org.everit.json.schema.internal.IPV4Validator;
import org.everit.json.schema.internal.IPV6Validator;
import org.everit.json.schema.internal.URIFormatValidator;

/**
 * Implementations perform the validation against the "format" keyword (see JSON Schema spec section
 * 7).
 */
public interface AbstractFormatValidator {
    
    /**
     * Implementation-specific validation of {@code subject}. If a validation error occurs then
     * implementations should return a programmer-friendly error message as a String wrapped in an
     * Optional. If the validation succeeded then {@link Optional#empty() an empty optional} should be
     * returned.
     *
     * @param subject
     *         the string to be validated
     * 
     * @return an {@code Optional} wrapping the error message if a validation error occured, otherwise
     * {@link Optional#empty() an empty optional}.
     */
    Optional<String> validate(String subject);

    /**
     * Implementation-specific validation of {@code subject}. If a validation error occurs then
     * implementations should return a programmer-friendly error message as a String wrapped in an
     * Optional. If the validation succeeded then {@link Optional#empty() an empty optional} should be
     * returned.
     *
     * @param subject
     *         the string to be validated
     * @param unprocessedProperties
     *         the map of unprocessed properties, which can be useful for some custom complex format
     * 
     * @return an {@code Optional} wrapping the error message if a validation error occured, otherwise
     * {@link Optional#empty() an empty optional}.
     */
    Optional<String> validate(String subject, Map<String, Object> unprocessedProperties);

    /**
     * Provides the name of this format.
     * <p>
     * Unless specified otherwise the {@link org.everit.json.schema.loader.SchemaLoader} will use this
     * name to recognize string schemas using this format.
     * </p>
     * The default implementation of this method returns {@code "unnamed-format"}. It is strongly
     * recommended for implementations to give a more meaningful name by overriding this method.
     *
     * @return the format name.
     */
    default String formatName() {
        return "unnamed-format";
    }

}
