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
package org.everit.json.schema.loader.internal;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Resolves an {@code id} or {@code ref} against a parent scope.
 * <p>
 * Used by TypeBasedMultiplexer (for handling <code>id</code>s) and by SchemaLoader (for handling
 * <code>ref</code>s).
 */
public final class ReferenceResolver {

    /**
     * Creates an absolute JSON pointer string based on a parent scope and a newly encountered pointer
     * segment ({@code id} or {@code ref} value).
     *
     * @param parentScope        the most immediate parent scope that the resolution should be performed against
     * @param encounteredSegment the new segment (complete URI, path, fragment etc) which must be resolved
     * @return the resolved URI
     */
    public static URI resolve(final URI parentScope, final String encounteredSegment) {
        try {
            return new URI(resolve(parentScope == null ? null : parentScope.toString(),
                    encounteredSegment));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an absolute JSON pointer string based on a parent scope and a newly encountered pointer
     * segment ({@code id} or {@code ref} value).
     *
     * @param parentScope        the most immediate parent scope that the resolution should be performed against
     * @param encounteredSegment the new segment (complete URI, path, fragment etc) which must be resolved
     * @return the resolved URI
     */
    public static String resolve(final String parentScope, final String encounteredSegment) {
        try {
            if (parentScope == null) {
                return encounteredSegment;
            }
            return new URI(parentScope).resolve(encounteredSegment).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private ReferenceResolver() {
    }

}
