package org.everit.json.schema;

import java.util.Optional;
import java.util.List;
import org.everit.json.schema.internal.JSONPrinter;

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

    private final Schema ifSchema;
    private final Schema thenSchema;
    private final Schema elseSchema;

    public ConditionalSchema(Builder builder) {
        super(builder);
        this.ifSchema = builder.ifSchema;
        this.thenSchema = builder.thenSchema;
        this.elseSchema = builder.elseSchema;
    }

    public Optional<Schema> getIfSchema() {
        return Optional.ofNullable(ifSchema);
    }

    public Optional<Schema> getThenSchema() {
        return Optional.ofNullable(thenSchema);
    }

    public Optional<Schema> getElseSchema() {
        return Optional.ofNullable(elseSchema);
    }

    @Override
    void accept(Visitor visitor, List<String> path) {
        visitor.visitConditionalSchema(this, path);
    }

    @Override void describePropertiesTo(JSONPrinter writer) {
        if (ifSchema != null) {
            writer.key("if");
            ifSchema.describeTo(writer);
        }
        if (thenSchema != null) {
            writer.key("then");
            thenSchema.describeTo(writer);
        }
        if (elseSchema != null) {
            writer.key("else");
            elseSchema.describeTo(writer);
        }
    }
}
