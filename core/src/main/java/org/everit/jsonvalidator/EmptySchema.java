package org.everit.jsonvalidator;

public class EmptySchema implements Schema {

  public static final EmptySchema INSTANCE = new EmptySchema();

  @Override
  public void validate(final Object subject) {
    // always true
  }

}
