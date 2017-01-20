/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import com.google.common.base.Preconditions;
import org.everit.json.schema.internal.*;


/**
 * Implementations perform the validation against the "format" keyword (see JSON Schema spec section
 * 7).
 */
public abstract class AbstractFormatValidator implements FormatValidator {

    /**
     * Static factory method for {@code FormatValidator} implementations supporting the
     * {@code formatName}s mandated by the json schema spec.
     * <p>
     * <ul>
     * <li>date-time</li>
     * <li>email</li>
     * <li>hostname</li>
     * <li>uri</li>
     * <li>ipv4</li>
     * <li>ipv6</li>
     * </ul>
     *
     * @param formatName one of the 6 built-in formats.
     * @return a {@code FormatValidator} implementation handling the {@code formatName} format.
     */
    static FormatValidator forFormat(final String formatName) {
        Preconditions.checkNotNull(formatName, "formatName cannot be null");
        if (formatName.equals("date-time")) {
            return new DateTimeFormatValidator();
        } else if (formatName.equals("email")) {
            return new EmailFormatValidator();
        } else if (formatName.equals("hostname")) {
            return new HostnameFormatValidator();
        } else if (formatName.equals("uri")) {
            return new URIFormatValidator();
        } else if (formatName.equals("ipv4")) {
            return new IPV4Validator();
        } else if (formatName.equals("ipv6")) {
            return new IPV6Validator();
        } else {
            throw new IllegalArgumentException("unsupported format: " + formatName);
        }
    }

    @Override
    public String formatName() {
        return "unnamed-format";
    }

}
