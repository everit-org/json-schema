package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.everit.json.schema.internal.JSONPrinter;

/**
 * {@code Not} schema validator.
 */
public class NotSchema extends Schema {

    /**
     * Builder class for {@link NotSchema}.
     */
    public static class Builder extends Schema.Builder<NotSchema> {

        private Schema mustNotMatch;

        @Override
        public NotSchema build() {
            return new NotSchema(this);
        }

        public Builder mustNotMatch(final Schema mustNotMatch) {
            this.mustNotMatch = mustNotMatch;
            return this;
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    private final Schema mustNotMatch;

    public NotSchema(final Builder builder) {
        super(builder);
        this.mustNotMatch = requireNonNull(builder.mustNotMatch, "mustNotMatch cannot be null");
    }

    public Schema getMustNotMatch() {
        return mustNotMatch;
    }

    @Override void accept(Visitor visitor) {
        visitor.visitNotSchema(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof NotSchema) {
            NotSchema that = (NotSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(mustNotMatch, that.mustNotMatch) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mustNotMatch);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NotSchema;
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) {
        writer.key("not");
        mustNotMatch.describeTo(writer);
    }
}
