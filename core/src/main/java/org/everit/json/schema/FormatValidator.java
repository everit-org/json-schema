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
public interface FormatValidator extends AbstractFormatValidator {

    /**
     * No-operation implementation (never throws {always returns {@link Optional#empty()}).
     */
    FormatValidator NONE = (subject) -> Optional.empty();

    /**
     * Static factory method for {@code FormatValidator} implementations supporting the
     * {@code formatName}s mandated by the json schema spec.
     * <ul>
     * <li>date-time</li>
     * <li>email</li>
     * <li>hostname</li>
     * <li>uri</li>
     * <li>ipv4</li>
     * <li>ipv6</li>
     * </ul>
     *
     * @param formatName
     *         one of the 6 built-in formats.
     * @return a {@code FormatValidator} implementation handling the {@code formatName} format.
     */
    static FormatValidator forFormat(final String formatName) {
        requireNonNull(formatName, "formatName cannot be null");
        switch (formatName) {
        case "date-time":
            return new DateTimeFormatValidator();
        case "email":
            return new EmailFormatValidator();
        case "hostname":
            return new HostnameFormatValidator();
        case "uri":
            return new URIFormatValidator();
        case "ipv4":
            return new IPV4Validator();
        case "ipv6":
            return new IPV6Validator();
        default:
            throw new IllegalArgumentException("unsupported format: " + formatName);
        }
    }

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
    default Optional<String> validate(String subject, Map<String, Object> unprocessedProperties) {
        return validate(subject);
    }

}
