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
@FunctionalInterface
public interface ContextualFormatValidator extends AbstractFormatValidator {

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
    default Optional<String> validate(String subject) {
        return validate(subject,null);
    }

}
