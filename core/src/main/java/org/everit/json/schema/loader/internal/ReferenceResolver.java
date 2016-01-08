package org.everit.json.schema.loader.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class ReferenceResolver {

  public static String resolve(final String parentScope, final String encounteredSegment) {
    return new ReferenceResolver(parentScope, encounteredSegment).resolve();
  }

  private final String parentScope;

  private final String encounteredSegment;

  public ReferenceResolver(final String parentScope, final String encounteredSegment) {
    this.parentScope = Objects.requireNonNull(parentScope, "parentScope cannot be null");
    this.encounteredSegment = Objects.requireNonNull(encounteredSegment,
        "encounteredSegment cannot be null");
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

  private String concat() {
    return parentScope + encounteredSegment;
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
