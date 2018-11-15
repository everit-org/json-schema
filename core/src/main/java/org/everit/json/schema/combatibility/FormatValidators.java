package org.everit.json.schema.combatibility;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.internal.*;

import static java8.util.Objects.requireNonNull;

public class FormatValidators {
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
    public static FormatValidator forFormat(final String formatName) {
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
}
