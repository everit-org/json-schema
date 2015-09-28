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
package org.everit.jsonvalidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;

/**
 * Array schema validator.
 *
 * {@link http://json-schema.org/latest/json-schema-validation.html#anchor36 See the according
 * specification}.
 */
public class ArraySchema extends Schema {

  /**
   * Builder class for {@link ArraySchema}.
   */
  public static class Builder extends Schema.Builder {

    private boolean requiresArray = true;

    private Integer minItems;

    private Integer maxItems;

    private boolean uniqueItems = false;

    private Schema allItemSchema;

    private List<Schema> itemSchemas = null;

    private boolean additionalItems = true;

    private Schema schemaOfAdditionalItems;

    /**
     * Adds an item schema for tuple validation.
     */
    public Builder addItemSchema(final Schema itemSchema) {
      if (itemSchemas == null) {
        itemSchemas = new ArrayList<Schema>();
      }
      itemSchemas.add(Objects.requireNonNull(itemSchema, "itemSchema cannot be null"));
      return this;
    }

    public Builder schemaOfAdditionalItems(final Schema schemaOfAdditionalItems) {
      this.schemaOfAdditionalItems = schemaOfAdditionalItems;
      return this;
    }

    public Builder requiresArray(final boolean requiresArray) {
      this.requiresArray = requiresArray;
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

  public boolean needsUniqueItems() {
    return uniqueItems;
  }

  public boolean permitsAdditionalItems() {
    return additionalItems;
  }

  private void testItemCount(final JSONArray subject) {
    int actualLength = subject.length();
    if (minItems != null && actualLength < minItems) {
      throw new ValidationException("expected minimum item count: " + minItems + ", found: "
          + actualLength);
    }
    if (maxItems != null && maxItems < actualLength) {
      throw new ValidationException("expected maximum item count: " + minItems + ", found: "
          + actualLength);
    }
  }

  private void testItems(final JSONArray subject) {
    if (allItemSchema != null) {
      for (int i = 0; i < subject.length(); ++i) {
        allItemSchema.validate(subject.get(i));
      }
    } else if (itemSchemas != null) {
      if (!additionalItems && subject.length() > itemSchemas.size()) {
        throw new ValidationException(String.format("expected: [%d] array items, found: [%d]",
            itemSchemas.size(), subject.length()));
      }
      int itemValidationUntil = Math.min(subject.length(), itemSchemas.size());
      for (int i = 0; i < itemValidationUntil; ++i) {
        itemSchemas.get(i).validate(subject.get(i));
      }
      if (schemaOfAdditionalItems != null) {
        for (int i = itemValidationUntil; i < subject.length(); ++i) {
          schemaOfAdditionalItems.validate(subject.get(i));
        }
      }
    }
  }

  private void testUniqueness(final JSONArray subject) {
    if (subject.length() == 0) {
      return;
    }
    Collection<Object> uniqueItems = new ArrayList<Object>(subject.length());
    for (int i = 0; i < subject.length(); ++i) {
      Object item = subject.get(i);
      for (Object contained : uniqueItems) {
        if (ObjectComparator.deepEquals(contained, item)) {
          throw new ValidationException("array items are not unique");
        }
      }
      uniqueItems.add(item);
    }
  }

  @Override
  public void validate(final Object subject) {
    if (!(subject instanceof JSONArray)) {
      if (requiresArray) {
        throw new ValidationException(JSONArray.class, subject);
      }
    } else {
      JSONArray arrSubject = (JSONArray) subject;
      testItemCount(arrSubject);
      if (uniqueItems) {
        testUniqueness(arrSubject);
      }
      testItems(arrSubject);
    }
  }

  public boolean requiresArray() {
    return requiresArray;
  }

  public Schema getSchemaOfAdditionalItems() {
    return schemaOfAdditionalItems;
  }

}
