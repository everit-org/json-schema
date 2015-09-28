package org.everit.jsonvalidator;

public class ReferenceSchema extends Schema {

  public static class Builder extends Schema.Builder {

    private ReferenceSchema retval;

    @Override
    public ReferenceSchema build() {
      if (retval == null) {
        retval = new ReferenceSchema(this);
      }
      return retval;
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  private Schema referredSchema;

  public ReferenceSchema(final Builder builder) {
    super(builder);
  }

  @Override
  void validate(final Object subject) {
    if (referredSchema == null) {
      throw new IllegalStateException("referredSchema must be injected before validation");
    }
    referredSchema.validate(subject);
  }

  public Schema getReferredSchema() {
    return referredSchema;
  }

  public void setReferredSchema(final Schema referredSchema) {
    if (this.referredSchema != null) {
      throw new IllegalStateException("referredSchema can be injected only once");
    }
    this.referredSchema = referredSchema;
  }

}
