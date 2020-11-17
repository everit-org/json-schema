package org.everit.json.schema;

import java.util.Objects;

/**
 * Number schema validator.
 */
public class NumberSchema extends Schema {

    /**
     * Builder class for {@link NumberSchema}.
     */
    public static class Builder extends Schema.Builder<NumberSchema, Builder> {

        private Number minimum;

        private Number maximum;

        private Number exclusiveMinimumLimit;

        private Number exclusiveMaximumLimit;

        private Number multipleOf;

        private boolean exclusiveMinimum = false;

        private boolean exclusiveMaximum = false;

        private boolean requiresNumber = true;

        private boolean requiresInteger = false;

        @Override
        protected Builder getBuilder()
        {
            return this;
        }

        @Override
        public NumberSchema build() {
            return new NumberSchema(this);
        }

        public Builder exclusiveMaximum(final boolean exclusiveMaximum) {
            this.exclusiveMaximum = exclusiveMaximum;
            return this;
        }

        public Builder exclusiveMinimum(final boolean exclusiveMinimum) {
            this.exclusiveMinimum = exclusiveMinimum;
            return this;
        }

        public Builder maximum(final Number maximum) {
            this.maximum = maximum;
            return this;
        }

        public Builder minimum(final Number minimum) {
            this.minimum = minimum;
            return this;
        }

        public Builder multipleOf(final Number multipleOf) {
            this.multipleOf = multipleOf;
            return this;
        }

        public Builder requiresInteger(final boolean requiresInteger) {
            this.requiresInteger = requiresInteger;
            return this;
        }

        public Builder requiresNumber(final boolean requiresNumber) {
            this.requiresNumber = requiresNumber;
            return this;
        }

        public Builder exclusiveMinimum(Number exclusiveMimimumLimit) {
            this.exclusiveMinimumLimit = exclusiveMimimumLimit;
            return this;
        }

        public Builder exclusiveMaximum(Number exclusiveMaximumLimit) {
            this.exclusiveMaximumLimit = exclusiveMaximumLimit;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final boolean requiresNumber;

    private final Number minimum;

    private final Number maximum;

    private final Number multipleOf;

    private final boolean exclusiveMinimum;

    private final boolean exclusiveMaximum;

    private final Number exclusiveMinimumLimit;

    private final Number exclusiveMaximumLimit;

    private final boolean requiresInteger;

    public NumberSchema() {
        this(builder());
    }

    /**
     * Constructor.
     *
     * @param builder
     *         the builder object containing validation criteria
     */
    public NumberSchema(final Builder builder) {
        super(builder);
        this.minimum = builder.minimum;
        this.maximum = builder.maximum;
        this.exclusiveMinimum = builder.exclusiveMinimum;
        this.exclusiveMaximum = builder.exclusiveMaximum;
        this.multipleOf = builder.multipleOf;
        this.requiresNumber = builder.requiresNumber;
        this.requiresInteger = builder.requiresInteger;
        this.exclusiveMinimumLimit = builder.exclusiveMinimumLimit;
        this.exclusiveMaximumLimit = builder.exclusiveMaximumLimit;
    }

    public Number getMaximum() {
        return maximum;
    }

    public Number getMinimum() {
        return minimum;
    }

    public Number getMultipleOf() {
        return multipleOf;
    }

    public boolean isExclusiveMaximum() {
        return exclusiveMaximum;
    }

    public boolean isExclusiveMinimum() {
        return exclusiveMinimum;
    }

    public boolean requiresInteger() {
        return requiresInteger;
    }

    public boolean isRequiresNumber() {
        return requiresNumber;
    }

    public Number getExclusiveMinimumLimit() {
        return exclusiveMinimumLimit;
    }

    public Number getExclusiveMaximumLimit() {
        return exclusiveMaximumLimit;
    }

    @Override void accept(Visitor visitor) {
        visitor.visitNumberSchema(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NumberSchema) {
            NumberSchema that = (NumberSchema) o;
            return that.canEqual(this) &&
                    requiresNumber == that.requiresNumber &&
                    exclusiveMinimum == that.exclusiveMinimum &&
                    exclusiveMaximum == that.exclusiveMaximum &&
                    Objects.equals(exclusiveMinimumLimit, that.exclusiveMinimumLimit) &&
                    Objects.equals(exclusiveMaximumLimit, that.exclusiveMaximumLimit) &&
                    requiresInteger == that.requiresInteger &&
                    Objects.equals(minimum, that.minimum) &&
                    Objects.equals(maximum, that.maximum) &&
                    Objects.equals(multipleOf, that.multipleOf) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(super.hashCode(), requiresNumber, minimum, maximum, multipleOf, exclusiveMinimum, exclusiveMaximum,
                        exclusiveMinimumLimit, exclusiveMaximumLimit, requiresInteger);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof NumberSchema;
    }
}
