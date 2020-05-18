package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Optional;

import org.everit.json.schema.internal.DateTimeFormatValidator;
import org.everit.json.schema.internal.EmailFormatValidator;
import org.everit.json.schema.internal.HostnameFormatValidator;
import org.everit.json.schema.internal.IPV4Validator;
import org.everit.json.schema.internal.IPV6Validator;
import org.everit.json.schema.internal.NoneFormatValidator;
import org.everit.json.schema.internal.URIFormatValidator;

/**
 * Implementations perform the validation against the "format" keyword (see JSON Schema spec section
 * 7).
 */
@FunctionalInterface
public interface FormatValidator {

    FormatValidator NONE = new NoneFormatValidator();

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
     * @return an {@code Optional} wrapping the error message if a validation error occured, otherwise
     * {@link Optional#empty() an empty optional}.
     */
    Optional<String> validate(String subject);

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
