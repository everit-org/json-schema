package org.everit.jsonvalidator;

public class SchemaException extends RuntimeException {
  private static final long serialVersionUID = 5987489689035036987L;

  public SchemaException(final String message) {
    super(message);
  }

  public SchemaException(final String key, final Class<?> expectedType, final Object actualValue) {
    super(String.format("key %s : expected type: %s , found : %s", key, expectedType
        .getSimpleName(), (actualValue == null ? "null" : actualValue.getClass().getSimpleName())));
  }

}
