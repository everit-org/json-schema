/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.everit.json.schema.internal.JSONPrinter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Validator for {@code allOf}, {@code oneOf}, {@code anyOf} schemas.
 */
public class CombinedSchema extends Schema {

    /**
     * Builder class for {@link CombinedSchema}.
     */
    public static class Builder extends Schema.Builder<CombinedSchema> {

        private ValidationCriterion criterion;

        private Collection<Schema> subschemas = new ArrayList<>();

        @Override
        public CombinedSchema build() {
            return new CombinedSchema(this);
        }

        public Builder criterion(final ValidationCriterion criterion) {
            this.criterion = criterion;
            return this;
        }

        public Builder subschema(final Schema subschema) {
            this.subschemas.add(subschema);
            return this;
        }

        public Builder subschemas(final Collection<Schema> subschemas) {
            this.subschemas = subschemas;
            return this;
        }

    }

    /**
     * Validation criterion.
     */
    public interface ValidationCriterion {

        /**
         * Throws a {@link ValidationException} if the implemented criterion is not fulfilled by the
         * {@code subschemaCount} and the {@code matchingSubschemaCount}.
         *
         * @param subschemaCount         the total number of checked subschemas
         * @param matchingSubschemaCount the number of subschemas which successfully validated the subject (did not throw
         *                               {@link ValidationException})
         */
        void validate(int subschemaCount, int matchingSubschemaCount);

    }

    /**
     * Validation criterion for {@code allOf} schemas.
     */
    public static final ValidationCriterion ALL_CRITERION = new ValidationCriterion() {

        @Override
        public void validate(int subschemaCount, int matchingCount) {
            if (matchingCount < subschemaCount) {
                throw new ValidationException(null,
                        format("only %d subschema matches out of %d", matchingCount, subschemaCount),
                        "allOf"
                );
            }
        }

        @Override
        public String toString() {
            return "allOf";
        }

    };

    /**
     * Validation criterion for {@code anyOf} schemas.
     */
    public static final ValidationCriterion ANY_CRITERION = new ValidationCriterion() {

        @Override
        public void validate(int subschemaCount, int matchingCount) {
            if (matchingCount == 0) {
                throw new ValidationException(null, format(
                        "no subschema matched out of the total %d subschemas",
                        subschemaCount), "anyOf");
            }
        }

        @Override
        public String toString() {
            return "anyOf";
        }
    };

    /**
     * Validation criterion for {@code oneOf} schemas.
     */
    public static final ValidationCriterion ONE_CRITERION =
            new ValidationCriterion() {

                @Override
                public void validate(int subschemaCount, int matchingCount) {
                    if (matchingCount != 1) {
                        throw new ValidationException(null, format("%d subschemas matched instead of one",
                                matchingCount), "oneOf");
                    }
                }

                @Override
                public String toString() {
                    return "oneOf";
                }
            };

    public static Builder allOf(final Collection<Schema> schemas) {
        return builder(schemas).criterion(ALL_CRITERION);
    }

    public static Builder anyOf(final Collection<Schema> schemas) {
        return builder(schemas).criterion(ANY_CRITERION);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(final Collection<Schema> subschemas) {
        return new Builder().subschemas(subschemas);
    }

    public static Builder oneOf(final Collection<Schema> schemas) {
        return builder(schemas).criterion(ONE_CRITERION);
    }

    private final Collection<Schema> subschemas;

    private final ValidationCriterion criterion;

    /**
     * Constructor.
     *
     * @param builder the builder containing the validation criterion and the subschemas to be checked
     */
    public CombinedSchema(final Builder builder) {
        super(builder);
        this.criterion = requireNonNull(builder.criterion, "criterion cannot be null");
        this.subschemas = requireNonNull(builder.subschemas, "subschemas cannot be null");
    }

    public ValidationCriterion getCriterion() {
        return criterion;
    }

    public Collection<Schema> getSubschemas() {
        return subschemas;
    }

    private ValidationException getFailure(final Schema schema, final Object subject) {
        try {
            schema.validate(subject);
            return null;
        } catch (ValidationException e) {
            return e;
        }
    }

    @Override
    public void validate(final Object subject) {
        List<ValidationException> failures = FluentIterable.from(subschemas)
                .transform(new Function<Schema, ValidationException>() {
                    @Override
                    public ValidationException apply(Schema schema) {
                        return getFailure(schema, subject);
                    }
                })
                .filter(Predicates.<ValidationException>notNull())
                .toList();
        int matchingCount = subschemas.size() - failures.size();
        try {
            criterion.validate(subschemas.size(), matchingCount);
        } catch (ValidationException e) {
            throw new ValidationException(this,
                    new StringBuilder(e.getPointerToViolation()),
                    e.getMessage(),
                    failures,
                    e.getKeyword());
        }
    }

    @Override
    public boolean definesProperty(final String field) {
        int matching = FluentIterable.from(subschemas)
                .filter(new Predicate<Schema>() {
                    @Override
                    public boolean apply(Schema schema) {
                        return schema.definesProperty(field);
                    }
                })
                .size();
        try {
            criterion.validate(subschemas.size(), matching);
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
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) {
        writer.key(criterion.toString());
        writer.array();
        for (Schema subschema : subschemas) {
            subschema.describeTo(writer);
        }
        writer.endArray();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subschemas, criterion);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof CombinedSchema;
    }
}
