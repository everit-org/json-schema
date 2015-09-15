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
package org.everit.jsonvalidator.loader;

import java.util.Objects;
import java.util.function.Consumer;

import org.everit.jsonvalidator.ArraySchema;
import org.everit.jsonvalidator.BooleanSchema;
import org.everit.jsonvalidator.IntegerSchema;
import org.everit.jsonvalidator.NullSchema;
import org.everit.jsonvalidator.Schema;
import org.everit.jsonvalidator.SchemaException;
import org.everit.jsonvalidator.StringSchema;
import org.json.JSONObject;

/**
 * Javadoc.
 *
 */
public class SchemaLoader {

  public static Schema load(final JSONObject schemaJson) {
    return new SchemaLoader(schemaJson).load();
  }

  private final JSONObject schemaJson;

  public SchemaLoader(final JSONObject schemaJson) {
    this.schemaJson = Objects.requireNonNull(schemaJson, "schemaJson cannot be null");
  }

  private Schema buildArraySchema() {
    ArraySchema.Builder builder = ArraySchema.builder();
    ifPresent("minItems", Integer.class, builder::minItems);
    ifPresent("maxItems", Integer.class, builder::maxItems);
    ifPresent("unique", Boolean.class, builder::uniqueItems);
    return builder.build();
  }

  private Schema buildIntegerSchema() {
    IntegerSchema.Builder builder = IntegerSchema.builder();
    ifPresent("minimum", Integer.class, builder::minimum);
    ifPresent("maximum", Integer.class, builder::maximum);
    ifPresent("multipleOf", Integer.class, builder::multipleOf);
    ifPresent("exclusiveMinimum", Boolean.class, builder::exclusiveMinimum);
    ifPresent("exclusiveMaximum", Boolean.class, builder::exclusiveMaximum);
    return builder.build();
  }

  private Schema buildStringSchema() {
    return new StringSchema(getInteger("minLength"), getInteger("maxLength"),
        getString("pattern"));
  }

  private Integer getInteger(final String key) {
    if (!schemaJson.has(key)) {
      return null;
    }
    Object rval = schemaJson.get(key);
    if (!(rval instanceof Integer)) {
      throw new SchemaException("expected Integer");
    }
    return (Integer) rval;
  }

  private String getString(final String key) {
    if (!schemaJson.has(key)) {
      return null;
    }
    return schemaJson.getString(key);
  }

  private <E> void ifPresent(final String key, final Class<E> expectedType,
      final Consumer<E> consumer) {
    if (schemaJson.has(key)) {
      E value = (E) schemaJson.get(key);
      try {
        consumer.accept(value);
      } catch (ClassCastException e) {
        throw new SchemaException(key, expectedType, value);
      }
    }
  }

  public Schema load() {
    String type = schemaJson.getString("type");
    switch (type) {
      case "string":
        return buildStringSchema();
      case "integer":
      case "number":
        return buildIntegerSchema();
      case "boolean":
        return BooleanSchema.INSTANCE;
      case "null":
        return NullSchema.INSTANCE;
      case "array":
        return buildArraySchema();
      default:
        throw new SchemaException(String.format("unknown type: [%s]", type));
    }
  }

}
