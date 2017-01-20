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
import com.google.common.base.Optional;
import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Array schema validator.
 */
public class ArraySchema extends Schema {

    /**
     * Builder class for {@link ArraySchema}.
     */
    public static class Builder extends Schema.Builder<ArraySchema> {

        private boolean requiresArray = true;

        private Integer minItems;

        private Integer maxItems;

        private boolean uniqueItems = false;

        private Schema allItemSchema;

        private List<Schema> itemSchemas = null;

        private boolean additionalItems = true;

        private Schema schemaOfAdditionalItems;

        /**
         * Adds an item schema for tuple validation. The array items of the subject under validation
         * will be matched to expected schemas by their index. In other words the {n}th
         * {@code addItemSchema()} invocation defines the expected schema of the {n}th item of the array
         * being validated.
         *
         * @param itemSchema the schema of the next item.
         * @return this
         */
        public Builder addItemSchema(final Schema itemSchema) {
            if (itemSchemas == null) {
                itemSchemas = new ArrayList<Schema>();
            }
            itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
            return this;
        }

        public Builder additionalItems(final boolean additionalItems) {
            this.additionalItems = additionalItems;
            return this;
        }

        public Builder allItemSchema(final Schema allItemSchema) {
            this.allItemSchema = allItemSchema;
            return this;
        }

        @Override
        public ArraySchema build() {
            return new ArraySchema(this);
        }

        public Builder maxItems(final Integer maxItems) {
            this.maxItems = maxItems;
            return this;
        }

        public Builder minItems(final Integer minItems) {
            this.minItems = minItems;
            return this;
        }

        public Builder requiresArray(final boolean requiresArray) {
            this.requiresArray = requiresArray;
            return this;
        }

        public Builder schemaOfAdditionalItems(final Schema schemaOfAdditionalItems) {
            this.schemaOfAdditionalItems = schemaOfAdditionalItems;
            return this;
        }

        public Builder uniqueItems(final boolean uniqueItems) {
            this.uniqueItems = uniqueItems;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Integer minItems;

    private final Integer maxItems;

    private final boolean uniqueItems;

    private final Schema allItemSchema;

    private final boolean additionalItems;

    private final List<Schema> itemSchemas;

    private final boolean requiresArray;

    private final Schema schemaOfAdditionalItems;

    /**
     * Constructor.
     *
     * @param builder contains validation criteria.
     */
    public ArraySchema(final Builder builder) {
        super(builder);
        this.minItems = builder.minItems;
        this.maxItems = builder.maxItems;
        this.uniqueItems = builder.uniqueItems;
        this.allItemSchema = builder.allItemSchema;
        this.itemSchemas = builder.itemSchemas;
        if (!builder.additionalItems && allItemSchema != null) {
            additionalItems = true;
        } else {
            additionalItems = builder.schemaOfAdditionalItems != null || builder.additionalItems;
        }
        this.schemaOfAdditionalItems = builder.schemaOfAdditionalItems;
        if (!(allItemSchema == null || itemSchemas == null)) {
            throw new SchemaException("cannot perform both tuple and list validation");
        }
        this.requiresArray = builder.requiresArray;
    }

    public Schema getAllItemSchema() {
        return allItemSchema;
    }

    public List<Schema> getItemSchemas() {
        return itemSchemas;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public Integer getMinItems() {
        return minItems;
    }

    public Schema getSchemaOfAdditionalItems() {
        return schemaOfAdditionalItems;
    }

    private Optional<ValidationException> ifFails(final Schema schema, final Object input) {
        try {
            schema.validate(input);
            return Optional.absent();
        } catch (ValidationException e) {
            return Optional.of(e);
        }
    }

    public boolean needsUniqueItems() {
        return uniqueItems;
    }

    public boolean permitsAdditionalItems() {
        return additionalItems;
    }

    public boolean requiresArray() {
        return requiresArray;
    }

    private Optional<ValidationException> testItemCount(final JSONArray subject) {
        int actualLength = subject.length();
        if (minItems != null && actualLength < minItems) {
            return Optional.of(new ValidationException(this, "expected minimum item count: " + minItems
                    + ", found: " + actualLength, "minItems"));
        }
        if (maxItems != null && maxItems < actualLength) {
            return Optional.of(new ValidationException(this, "expected maximum item count: " + minItems
                    + ", found: " + actualLength, "maxItems"));
        }
        return Optional.absent();
    }

    private List<ValidationException> testItems(final JSONArray subject) {
        List<ValidationException> rval = new ArrayList<>();
        if (allItemSchema != null) {
            validateItemsAgainstSchema(subject.length(), subject, allItemSchema, rval);
        } else if (itemSchemas != null) {
            if (!additionalItems && subject.length() > itemSchemas.size()) {
                rval.add(new ValidationException(this, String.format(
                        "expected: [%d] array items, found: [%d]",
                        itemSchemas.size(), subject.length()), "items"));
            }
            int itemValidationUntil = Math.min(subject.length(), itemSchemas.size());
            validateItemsAgainstSchema(itemValidationUntil, subject, new Function<Integer, Schema>() {
                @Override
                public Schema apply(Integer input) {
                    return itemSchemas.get(input);
                }
            }, rval);
            if (schemaOfAdditionalItems != null) {
                validateItemsAgainstSchema(itemValidationUntil, subject.length(), subject, schemaOfAdditionalItems, rval);
            }
        }
        return rval;
    }

    private void validateItemsAgainstSchema(final int endExclusive, final JSONArray items, final Schema schema,
            final List<ValidationException> rval) {
        validateItemsAgainstSchema(0, endExclusive, items, schema, rval);
    }

    private void validateItemsAgainstSchema(final int startInclusive, final int endExclusive, final JSONArray items,
            final Schema schema,
            final List<ValidationException> rval) {
        validateItemsAgainstSchema(startInclusive, endExclusive, items, new Function<Integer, Schema>() {
            @Override
            public Schema apply(Integer input) {
                return schema;
            }
        }, rval);
    }

    private void validateItemsAgainstSchema(final int endExclusive, final JSONArray items,
            final Function<Integer, Schema> schemaForIndex, final List<ValidationException> rval) {
        validateItemsAgainstSchema(0, endExclusive, items, schemaForIndex, rval);
    }

    private void validateItemsAgainstSchema(final int startInclusive, final int endExclusive, final JSONArray items,
            final Function<Integer, Schema> schemaForIndex, final List<ValidationException> rval) {
        for (int i = startInclusive; i < endExclusive; i++) {
            final String copyOfI = String.valueOf(i); // i is not effectively final so we copy it
            Optional<ValidationException> maybeException = ifFails(schemaForIndex.apply(i), items.get(i))
                    .transform(new Function<ValidationException, ValidationException>() {
                        @Override
                        public ValidationException apply(ValidationException exc) {
                            return exc.prepend(copyOfI);
                        }
                    });

            if (maybeException.isPresent()) {
                rval.add(maybeException.get());
            }
        }
    }

    private Optional<ValidationException> testUniqueness(final JSONArray subject) {
        if (subject.length() == 0) {
            return Optional.absent();
        }
        Collection<Object> uniqueItems = new ArrayList<Object>(subject.length());
        for (int i = 0; i < subject.length(); ++i) {
            Object item = subject.get(i);
            for (Object contained : uniqueItems) {
                if (ObjectComparator.deepEquals(contained, item)) {
                    return Optional.of(
                            new ValidationException(this, "array items are not unique", "uniqueItems"));
                }
            }
            uniqueItems.add(item);
        }
        return Optional.absent();
    }

    @Override
    public void validate(final Object subject) {
        List<ValidationException> failures = new ArrayList<>();
        if (!(subject instanceof JSONArray)) {
            if (requiresArray) {
                throw new ValidationException(this, JSONArray.class, subject);
            }
        } else {
            JSONArray arrSubject = (JSONArray) subject;
            failures.addAll(testItemCount(arrSubject).asSet());
            if (uniqueItems) {
                failures.addAll(testUniqueness(arrSubject).asSet());
            }
            failures.addAll(testItems(arrSubject));
        }
        ValidationException.throwFor(this, failures);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ArraySchema) {
            ArraySchema that = (ArraySchema) o;
            return that.canEqual(this) &&
                    uniqueItems == that.uniqueItems &&
                    additionalItems == that.additionalItems &&
                    requiresArray == that.requiresArray &&
                    Objects.equals(minItems, that.minItems) &&
                    Objects.equals(maxItems, that.maxItems) &&
                    Objects.equals(allItemSchema, that.allItemSchema) &&
                    Objects.equals(itemSchemas, that.itemSchemas) &&
                    Objects.equals(schemaOfAdditionalItems, that.schemaOfAdditionalItems) &&
                    super.equals(o);
        } else {
            return false;
        }
    }

    @Override
    void describePropertiesTo(final JSONPrinter writer) {
        if (requiresArray) {
            writer.key("type").value("array");
        }
        writer.ifTrue("uniqueItems", uniqueItems);
        writer.ifPresent("minItems", minItems);
        writer.ifPresent("maxItems", maxItems);
        writer.ifFalse("additionalItems", additionalItems);
        if (allItemSchema != null) {
            writer.key("items");
            allItemSchema.describeTo(writer);
        }
        if (itemSchemas != null) {
            writer.key("items");
            writer.array();
            for (Schema schema : itemSchemas) {
                schema.describeTo(writer);
            }
            writer.endArray();
        }
        if (schemaOfAdditionalItems != null) {
            writer.key("additionalItems");
            schemaOfAdditionalItems.describeTo(writer);
        }
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof ArraySchema;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minItems, maxItems, uniqueItems, allItemSchema,
                additionalItems, itemSchemas, requiresArray, schemaOfAdditionalItems);
    }
}
