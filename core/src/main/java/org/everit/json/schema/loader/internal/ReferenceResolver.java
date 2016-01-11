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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Resolves an {@code id} or {@code ref} against a parent scope.
 *
 * Used by TypeBasedMultiplexer (for handling <code>id</code>s) and by SchemaLoader (for handling
 * <code>ref</code>s).
 */
public final class ReferenceResolver {

  /**
   * Creates an absolute JSON pointer string based on a parent scope and a newly encountered pointer
   * segment ({@code id} or {@code ref} value).
   *
   * @param parentScope
   *          the most immediate parent scope that the resolution should be performed against
   * @param encounteredSegment
   *          the new segment (complete URI, path, fragment etc) which must be resolved
   * @return the resolved URI
   */
  public static String resolve(final String parentScope, final String encounteredSegment) {
    return new ReferenceResolver(parentScope, encounteredSegment).resolve();
  }

  private final String parentScope;

  private final String encounteredSegment;

  private ReferenceResolver(final String parentScope, final String encounteredSegment) {
    this.parentScope = Objects.requireNonNull(parentScope, "parentScope cannot be null");
    this.encounteredSegment = Objects.requireNonNull(encounteredSegment,
        "encounteredSegment cannot be null");
  }

  private String concat() {
    return parentScope + encounteredSegment;
  }

  private String handlePathIdAttr() {
    try {
      URL parentScopeURL = new URL(parentScope);
      StringBuilder newIdBuilder = new StringBuilder().append(parentScopeURL.getProtocol())
          .append("://")
          .append(parentScopeURL.getHost());
      if (parentScopeURL.getPort() > -1) {
        newIdBuilder.append(":").append(parentScopeURL.getPort());
      }
      newIdBuilder.append("/").append(encounteredSegment);
      return newIdBuilder.toString();
    } catch (MalformedURLException e1) {
      return concat();
    }
  }

  private String nonfragmentIdAttr() {
    try {
      URL url = new URL(encounteredSegment);
      return url.toExternalForm();
    } catch (MalformedURLException e) {
      return handlePathIdAttr();
    }
  }

  private String resolve() {
    if (encounteredSegment.startsWith("#")) {
      return concat();
    }
    return nonfragmentIdAttr();
  }

}
