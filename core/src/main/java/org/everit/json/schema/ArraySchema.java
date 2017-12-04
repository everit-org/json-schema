package org.everit.json.schema;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONArray;

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

        private Schema containedItemSchema;

        /**
         * Adds an item schema for tuple validation. The array items of the subject under validation
         * will be matched to expected schemas by their index. In other words the {n}th
         * {@code addItemSchema()} invocation defines the expected schema of the {n}th item of the array
         * being validated.
         *
         * @param itemSchema
         *         the schema of the next item.
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

        public Builder containsItemSchema(Schema contained) {
            this.containedItemSchema = contained;
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

    private final Schema containedItemSchema;

    /**
     * Constructor.
     *
     * @param builder
     *         contains validation criteria.
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
        this.containedItemSchema = builder.containedItemSchema;
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

    public Schema getContainedItemSchema() {
        return containedItemSchema;
    }

    private Optional<ValidationException> ifFails(final Schema schema, final Object input) {
        try {
            schema.validate(input);
            return Optional.empty();
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

    private void testItemCount(final JSONArray subject, List<ValidationException> validationExceptions) {
        int actualLength = subject.length();
        if (minItems != null && actualLength < minItems) {
            validationExceptions.add(
                    failure("expected minimum item count: " + minItems
                    + ", found: " + actualLength, "minItems"));
            return;
        }
        if (maxItems != null && maxItems < actualLength) {
            validationExceptions.add(
                    failure("expected maximum item count: " + maxItems
                    + ", found: " + actualLength, "maxItems"));
        }
    }

    private void testItems(final JSONArray subject, List<ValidationException> validationExceptions) {
        if (allItemSchema != null) {
            validateItemsAgainstSchema(IntStream.range(0, subject.length()),
                    subject,
                    allItemSchema,
                    validationExceptions::add);
        } else if (itemSchemas != null) {
            if (!additionalItems && subject.length() > itemSchemas.size()) {
                validationExceptions.add(
                        failure(format("expected: [%d] array items, found: [%d]",
                        itemSchemas.size(), subject.length()), "items"));
            }
            int itemValidationUntil = Math.min(subject.length(), itemSchemas.size());
            validateItemsAgainstSchema(IntStream.range(0, itemValidationUntil),
                    subject,
                    itemSchemas::get,
                    validationExceptions::add);
            if (schemaOfAdditionalItems != null) {
                validateItemsAgainstSchema(IntStream.range(itemValidationUntil, subject.length()),
                        subject,
                        schemaOfAdditionalItems,
                        validationExceptions::add);
            }
        }
    }

    private void validateItemsAgainstSchema(final IntStream indices, final JSONArray items,
            final Schema schema,
            final Consumer<ValidationException> failureCollector) {
        validateItemsAgainstSchema(indices, items, i -> schema, failureCollector);
    }

    private void validateItemsAgainstSchema(final IntStream indices, final JSONArray items,
            final IntFunction<Schema> schemaForIndex,
            final Consumer<ValidationException> failureCollector) {
        for (int i : indices.toArray()) {
            String copyOfI = String.valueOf(i); // i is not effectively final so we copy it
            ifFails(schemaForIndex.apply(i), items.get(i))
                    .map(exc -> exc.prepend(copyOfI))
                    .ifPresent(failureCollector);
        }
    }

    private void testUniqueness(final JSONArray subject, List<ValidationException> validationExceptions) {
        if (subject.length() == 0) {
            return;
        }
        Collection<Object> uniqueItems = new ArrayList<Object>(subject.length());
        for (int i = 0; i < subject.length(); ++i) {
            Object item = subject.get(i);
            for (Object contained : uniqueItems) {
                if (ObjectComparator.deepEquals(contained, item)) {
                    validationExceptions.add(
                            failure("array items are not unique", "uniqueItems"));
                    return;
                }
            }
            uniqueItems.add(item);
        }
    }

    @Override
    public void validate(final Object subject) {
        if (checkNullity(subject)) {
            if (!(subject instanceof JSONArray)) {
                if (requiresArray) {
                    throw failure(JSONArray.class, subject);
                }
            } else {
                List<ValidationException> validationExceptions = new ArrayList<>();
                JSONArray arrSubject = (JSONArray) subject;
                testItemCount(arrSubject, validationExceptions);
                if (uniqueItems) {
                    testUniqueness(arrSubject, validationExceptions);
                }
                testItems(arrSubject, validationExceptions);
                testContains(arrSubject, validationExceptions);
                if (null != validationExceptions) {
                    ValidationException.throwFor(this, validationExceptions);
                }
            }
        }
    }

    private void testContains(JSONArray arrSubject, List<ValidationException> validationExceptions) {
        if (containedItemSchema == null) {
            return;
        }
        for (int i = 0; i < arrSubject.length(); i++) {
            Optional<ValidationException> exception = ifFails(containedItemSchema, arrSubject.get(i));
            if (!exception.isPresent()) {
                return;
            }
        }
        validationExceptions.add(
                failure("expected at least one array item to match 'contains' schema", "contains"));
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
                    Objects.equals(containedItemSchema, that.containedItemSchema) &&
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
            itemSchemas.forEach(schema -> schema.describeTo(writer));
            writer.endArray();
        }
        if (schemaOfAdditionalItems != null) {
            writer.key("additionalItems");
            schemaOfAdditionalItems.describeTo(writer);
        }
        if (containedItemSchema != null) {
            writer.key("contains");
            containedItemSchema.describeTo(writer);
        }
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof ArraySchema;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minItems, maxItems, uniqueItems, allItemSchema,
                additionalItems, itemSchemas, requiresArray, schemaOfAdditionalItems, containedItemSchema);
    }
}
