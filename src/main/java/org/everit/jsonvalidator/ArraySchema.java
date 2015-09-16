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
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.json.JSONArray;

public class ArraySchema implements Schema {

  public static class Builder {

    private Integer minItems;

    private Integer maxItems;

    private boolean uniqueItems = false;

    private Schema allItemSchema;

    private List<Schema> itemSchemas = null;

    private boolean additionalItems = true;

    public Builder addItemSchema(final Schema itemSchema) {
      if (itemSchemas == null) {
        itemSchemas = new ArrayList<Schema>();
      }
      itemSchemas.add(Objects.requireNonNull(itemSchema, "itemSchema cannot be null"));
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

  public ArraySchema(final Builder builder) {
    this.minItems = builder.minItems;
    this.maxItems = builder.maxItems;
    this.uniqueItems = builder.uniqueItems;
    this.allItemSchema = builder.allItemSchema;
    this.additionalItems = builder.additionalItems;
    this.itemSchemas = builder.itemSchemas;
  }

  public Integer getMaxItems() {
    return maxItems;
  }

  public Integer getMinItems() {
    return minItems;
  }

  public boolean isUniqueItems() {
    return uniqueItems;
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
      for (int i = 0; i < subject.length(); ++i) {
        itemSchemas.get(i).validate(subject.get(i));
      }
    }
  }

  private void testUniqueness(final JSONArray subject) {
    if (subject.length() == 0) {
      return;
    }
    long uniqueLength = IntStream.range(0, subject.length())
        .mapToObj(subject::get)
        .map(Object::toString)
        .distinct()
        .count();
    if (uniqueLength < subject.length()) {
      throw new ValidationException("array items are not unique");
    }
  }

  @Override
  public void validate(final Object subject) {
    if (!(subject instanceof JSONArray)) {
      throw new ValidationException(JSONArray.class, subject);
    }
    JSONArray arrSubject = (JSONArray) subject;
    testItemCount(arrSubject);
    if (uniqueItems) {
      testUniqueness(arrSubject);
    }
    testItems(arrSubject);
  }

}
