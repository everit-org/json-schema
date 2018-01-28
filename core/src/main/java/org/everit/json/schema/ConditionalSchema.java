package org.everit.json.schema;

/**
 * Validator for {@code if}, {@code then}, {@code else} schemas.
 */
public class ConditionalSchema extends Schema {

    /**
     * Builder class for {@link ConditionalSchema}.
     */
    public static class Builder extends Schema.Builder<ConditionalSchema> {
        private Schema ifSchema;
        private Schema thenSchema;
        private Schema elseSchema;

        public Builder ifSchema(final Schema ifSchema) {
            this.ifSchema = ifSchema;
            return this;
        }

        public Builder thenSchema(final Schema thenSchema) {
            this.thenSchema = thenSchema;
            return this;
        }

        public Builder elseSchema(final Schema elseSchema) {
            this.elseSchema = elseSchema;
            return this;
        }

        @Override
        public ConditionalSchema build() {
            return new ConditionalSchema(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public ConditionalSchema(Builder builder) {
        super(builder);
    }

    @Override
    void accept(Visitor visitor) {

    }
}
