package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.Objects;

import static java.lang.String.format;

/**
 * Number schema validator.
 */
public class NumberSchema extends Schema {

    /**
     * Builder class for {@link NumberSchema}.
     */
    public static class Builder extends Schema.Builder<NumberSchema> {

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
     * @param builder the builder object containing validation criteria
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

    private void checkMaximum(final double subject) {
        if (maximum != null) {
            if (exclusiveMaximum && maximum.doubleValue() <= subject) {
                throw failure(subject + " is not less than " + maximum, "exclusiveMaximum");
            } else if (maximum.doubleValue() < subject) {
                throw failure(subject + " is not less or equal to " + maximum, "maximum");
            }
        }
        if (exclusiveMaximumLimit != null) {
            if (subject >= exclusiveMaximumLimit.doubleValue()) {
                throw failure(format("is not less than " + exclusiveMaximumLimit), "exclusiveMaximum");
            }
        }
    }

    private void checkMinimum(final double subject) {
        if (minimum != null) {
            if (exclusiveMinimum && subject <= minimum.doubleValue()) {
                throw failure(subject + " is not greater than " + minimum, "exclusiveMinimum");
            } else if (subject < minimum.doubleValue()) {
                throw failure(subject + " is not greater or equal to " + minimum, "minimum");
            }
        }
        if (exclusiveMinimumLimit != null) {
            if (subject <= exclusiveMinimumLimit.doubleValue()) {
                throw failure(subject + " is not greater than " + exclusiveMinimumLimit, "exclusiveMinimum");
            }
        }
    }

    private void checkMultipleOf(final double subject) {
        if (multipleOf != null) {
            BigDecimal remainder = BigDecimal.valueOf(subject).remainder(
                    BigDecimal.valueOf(multipleOf.doubleValue()));
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                throw failure(subject + " is not a multiple of " + multipleOf, "multipleOf");
            }
        }
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

    @Override
    public void validate(final Object subject) {
        if (!(subject instanceof Number)) {
            if (requiresNumber) {
                throw failure(Number.class, subject);
            }
        } else {
            if (!(subject instanceof Integer || subject instanceof Long) && requiresInteger) {
                throw failure(Integer.class, subject);
            }
            double intSubject = ((Number) subject).doubleValue();
            checkMinimum(intSubject);
            checkMaximum(intSubject);
            checkMultipleOf(intSubject);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
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
    void describePropertiesTo(JSONPrinter writer) {
        if (requiresInteger) {
            writer.key("type").value("integer");
        } else if (requiresNumber) {
            writer.key("type").value("number");
        }
        writer.ifPresent("minimum", minimum);
        writer.ifPresent("maximum", maximum);
        writer.ifPresent("multipleOf", multipleOf);
        writer.ifTrue("exclusiveMinimum", exclusiveMinimum);
        writer.ifTrue("exclusiveMaximum", exclusiveMaximum);
        try {
            writer.ifPresent("exclusiveMinimum", exclusiveMinimumLimit);
            writer.ifPresent("exclusiveMaximum", exclusiveMaximumLimit);
        } catch (JSONException e) {
            throw new IllegalStateException("overloaded use of exclusiveMinimum or exclusiveMaximum keyword");
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
