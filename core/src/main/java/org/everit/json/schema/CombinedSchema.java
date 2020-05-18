package org.everit.json.schema;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Validator for {@code allOf}, {@code oneOf}, {@code anyOf} schemas.
 */
public class CombinedSchema extends Schema {

    /**
     * Validation criterion for {@code allOf} schemas.
     */
    public static final ValidationCriterion ALL_CRITERION = new ValidationCriterionAll();

    /**
     * Validation criterion for {@code anyOf} schemas.
     */
    public static final ValidationCriterion ANY_CRITERION = new ValidationCriterionAny();

    /**
     * Validation criterion for {@code oneOf} schemas.
     */
    public static final ValidationCriterion ONE_CRITERION = new ValidationCriterionOne();

    /**
     * Builder class for {@link CombinedSchema}.
     */
    public static class Builder extends Schema.Builder<CombinedSchema> {

        private ValidationCriterion criterion;

        private Collection<Schema> subschemas = new ArrayList<>();

        private boolean synthetic;

        @Override
        public CombinedSchema build() {
            return new CombinedSchema(this);
        }

        public Builder criterion(ValidationCriterion criterion) {
            this.criterion = criterion;
            return this;
        }

        public Builder subschema(Schema subschema) {
            this.subschemas.add(subschema);
            return this;
        }

        public Builder subschemas(Collection<Schema> subschemas) {
            this.subschemas = subschemas;
            return this;
        }

        public Builder isSynthetic(boolean synthetic) {
            this.synthetic = synthetic;
            return this;
        }
    }

    public static Builder allOf(Collection<Schema> schemas) {
        return builder(schemas).criterion(ALL_CRITERION);
    }

    public static Builder anyOf(Collection<Schema> schemas) {
        return builder(schemas).criterion(ANY_CRITERION);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Collection<Schema> subschemas) {
        return new Builder().subschemas(subschemas);
    }

    public static Builder oneOf(Collection<Schema> schemas) {
        return builder(schemas).criterion(ONE_CRITERION);
    }

    private final boolean synthetic;

    private final Collection<Schema> subschemas;

    private final ValidationCriterion criterion;

    /**
     * Constructor.
     *
     * @param builder
     *         the builder containing the validation criterion and the subschemas to be checked
     */
    public CombinedSchema(Builder builder) {
        super(builder);
        this.synthetic = builder.synthetic;
        this.criterion = requireNonNull(builder.criterion, "criterion cannot be null");
        this.subschemas = requireNonNull(builder.subschemas, "subschemas cannot be null");
    }

    public ValidationCriterion getCriterion() {
        return criterion;
    }

    public Collection<Schema> getSubschemas() {
        return subschemas;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    @Override void accept(Visitor visitor) {
        visitor.visitCombinedSchema(this);
    }

    @Override
    public boolean definesProperty(String field) {
        List<Schema> matching = new ArrayList<>();
        for (Schema subschema : subschemas) {
            if (subschema.definesProperty(field)) {
                matching.add(subschema);
            }
        }
        try {
            criterion.validate(subschemas.size(), matching.size());
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CombinedSchema) {
            CombinedSchema that = (CombinedSchema) o;
            return that.canEqual(this) &&
                    Objects.equals(subschemas, that.subschemas) &&
                    Objects.equals(criterion, that.criterion) &&
                    synthetic == that.synthetic &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subschemas, criterion, synthetic);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof CombinedSchema;
    }
}
