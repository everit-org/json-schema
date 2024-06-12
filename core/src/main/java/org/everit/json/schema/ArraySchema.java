package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.everit.json.schema.internal.JSONPrinter;

/**
 * Array schema validator.
 */
public class ArraySchema
        extends Schema {

    /**
     * Builder class for {@link ArraySchema}.
     */
    public static class Builder
            extends Schema.Builder<ArraySchema, Builder> {

        private boolean requiresArray = true;

        private Integer minItems;

        private Integer maxItems;

        private boolean uniqueItems = false;

        private Schema allItemSchema;

        private List<Schema> itemSchemas = null;

        private boolean additionalItems = true;

        private Schema schemaOfAdditionalItems;

        private Schema containedItemSchema;

        @Override
        protected Builder getBuilder()
        {
            return this;
        }

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

    public boolean needsUniqueItems() {
        return uniqueItems;
    }

    public boolean permitsAdditionalItems() {
        return additionalItems;
    }

    public boolean requiresArray() {
        return requiresArray;
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
    void accept(Visitor visitor) {
        visitor.visitArraySchema(this);
    }

    @Override
    public boolean definesProperty(String field) {
        String[] headAndTail = headAndTailOfJsonPointerFragment(field);
        String nextToken = headAndTail[0];
        String remaining = headAndTail[1];
        boolean hasRemaining = remaining != null;
        try {
            return tryPropertyDefinitionByNumericIndex(nextToken, remaining, hasRemaining);
        } catch (NumberFormatException e) {
            return tryPropertyDefinitionByMetaIndex(nextToken, remaining, hasRemaining);
        }
    }

    private boolean tryPropertyDefinitionByMetaIndex(String nextToken, String remaining, boolean hasRemaining) {
        boolean isAll = "all".equals(nextToken);
        boolean isAny = "any".equals(nextToken);
        if (!hasRemaining && (isAll || isAny)) {
            return true;
        }
        if (isAll) {
            if (allItemSchema != null) {
                return allItemSchema.definesProperty(remaining);
            } else {
                boolean allItemSchemasDefine = itemSchemas.stream()
                        .map(schema -> schema.definesProperty(remaining))
                        .reduce(true, Boolean::logicalAnd);
                if (allItemSchemasDefine) {
                    if (schemaOfAdditionalItems != null) {
                        return schemaOfAdditionalItems.definesProperty(remaining);
                    } else {
                        return true;
                    }
                }
                return false;
            }
        } else if (isAny) {
            if (allItemSchema != null) {
                return allItemSchema.definesProperty(remaining);
            } else {
                boolean anyItemSchemasDefine = itemSchemas.stream()
                        .map(schema -> schema.definesProperty(remaining))
                        .reduce(false, Boolean::logicalOr);
                return anyItemSchemasDefine
                        || (schemaOfAdditionalItems == null || schemaOfAdditionalItems.definesProperty(remaining));
            }
        }
        return false;
    }

    private boolean tryPropertyDefinitionByNumericIndex(String nextToken, String remaining, boolean hasRemaining) {
        int index = Integer.parseInt(nextToken);
        if (index < 0) {
            return false;
        }
        if (maxItems != null && maxItems <= index) {
            return false;
        }
        if (allItemSchema != null && hasRemaining) {
            return allItemSchema.definesProperty(remaining);
        } else {
            if (hasRemaining) {
                if (index < itemSchemas.size()) {
                    return itemSchemas.get(index).definesProperty(remaining);
                }
                if (schemaOfAdditionalItems != null) {
                    return schemaOfAdditionalItems.definesProperty(remaining);
                }
            }
            return additionalItems;
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
