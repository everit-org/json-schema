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
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.everit.jsonvalidator.BooleanSchema;
import org.everit.jsonvalidator.IntegerSchema;
import org.everit.jsonvalidator.IntegerSchema.Builder;
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

  private Schema buildIntegerSchema() {
    Builder builder = IntegerSchema.builder();
    ifPresent("minimum", JSONObject::getInt, builder::minimum);
    ifPresent("maximum", JSONObject::getInt, builder::maximum);
    ifPresent("multipleOf", JSONObject::getInt, builder::multipleOf);
    ifPresent("exclusiveMinimum", JSONObject::getBoolean, builder::exclusiveMinimum);
    ifPresent("exclusiveMaximum", JSONObject::getBoolean, builder::exclusiveMaximum);
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

  private <E> void ifPresent(final String key, final BiFunction<JSONObject, String, E> getter,
      final Consumer<E> consumer) {
    if (schemaJson.has(key)) {
      E value = getter.apply(schemaJson, key);
      consumer.accept(value);
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
      default:
        throw new SchemaException(String.format("unknown type: [%s]", type));
    }
  }

}
