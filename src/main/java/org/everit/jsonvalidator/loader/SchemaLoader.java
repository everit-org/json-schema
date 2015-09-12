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

import org.json.JSONObject;

/**
 * Javadoc.
 *
 * @param <S>
 *          Generic parameter.
 */
public interface SchemaLoader<S> {

  /**
   * Factory.
   */
  static <S> SchemaLoader<S> of(final JSONObject jsonSchema) {
    String type = jsonSchema.getString("type");
    switch (type) {
      case "string":
        return (SchemaLoader<S>) new StringSchemaLoader();
      case "object":
        return (SchemaLoader<S>) new ObjectSchemaLoader();
      case "array":
        return (SchemaLoader<S>) new ArraySchemaLoader();
      case "boolean":
        return (SchemaLoader<S>) new BooleanSchemaLoader();
      case "null":
        return (SchemaLoader<S>) new NullSchemaLoader();
      case "number":
      case "integer":
        return (SchemaLoader<S>) new IntegerSchemaLoader();
      default:
        throw new IllegalArgumentException("unknown type: " + type);
    }
  }
}
