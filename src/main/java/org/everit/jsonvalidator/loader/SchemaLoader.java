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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.everit.jsonvalidator.ArraySchema;
import org.everit.jsonvalidator.BooleanSchema;
import org.everit.jsonvalidator.CombinedSchema;
import org.everit.jsonvalidator.IntegerSchema;
import org.everit.jsonvalidator.NotSchema;
import org.everit.jsonvalidator.NullSchema;
import org.everit.jsonvalidator.ObjectSchema;
import org.everit.jsonvalidator.ObjectSchema.Builder;
import org.everit.jsonvalidator.Schema;
import org.everit.jsonvalidator.SchemaException;
import org.everit.jsonvalidator.StringSchema;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Javadoc.
 *
 */
public class SchemaLoader {

  public static Schema load(final JSONObject schemaJson) {
    return new SchemaLoader(schemaJson, schemaJson).load();
  }

  private final JSONObject schemaJson;

  private final JSONObject rootSchemaJson;

  private static final Map<String, Function<Collection<Schema>, CombinedSchema>> //
  COMBINED_SUBSCHEMA_PROVIDERS = new HashMap<>(3);

  static {
    COMBINED_SUBSCHEMA_PROVIDERS.put("allOf", CombinedSchema::allOf);
    COMBINED_SUBSCHEMA_PROVIDERS.put("anyOf", CombinedSchema::anyOf);
    COMBINED_SUBSCHEMA_PROVIDERS.put("oneOf", CombinedSchema::oneOf);
  }

  public SchemaLoader(final JSONObject schemaJson, final JSONObject rootSchemaJson) {
    this.schemaJson = Objects.requireNonNull(schemaJson, "schemaJson cannot be null");
    this.rootSchemaJson = Objects.requireNonNull(rootSchemaJson, "rootSchemaJson cannot be null");
  }

  private void addDependencies(final Builder builder, final JSONObject deps) {
    Arrays.stream(JSONObject.getNames(deps))
        .forEach(ifPresent -> addDependency(builder, ifPresent, deps.get(ifPresent)));
  }

  private Schema loadChild(final JSONObject childJson) {
    return new SchemaLoader(childJson, rootSchemaJson).load();
  }

  private Object addDependency(final Builder builder, final String ifPresent, final Object deps) {
    if (deps instanceof JSONObject) {
      Schema schema = loadChild((JSONObject) deps);
      builder.schemaDependency(ifPresent, schema);
    } else if (deps instanceof JSONArray) {
      JSONArray propNames = (JSONArray) deps;
      IntStream.range(0, propNames.length())
          .mapToObj(i -> propNames.getString(i))
          .forEach(dependency -> builder.propertyDependency(ifPresent, dependency));
    } else {
      throw new SchemaException(String.format(
          "values in 'dependencies' must be arrays or objects, found [%s]", deps.getClass()
              .getSimpleName()));
    }
    return null;
  }

  private Schema buildArraySchema() {
    ArraySchema.Builder builder = ArraySchema.builder();
    ifPresent("minItems", Integer.class, builder::minItems);
    ifPresent("maxItems", Integer.class, builder::maxItems);
    ifPresent("unique", Boolean.class, builder::uniqueItems);
    ifPresent("additionalItems", Boolean.class, builder::additionalItems);
    if (schemaJson.has("items")) {
      Object itemSchema = schemaJson.get("items");
      if (itemSchema instanceof JSONObject) {
        builder.allItemSchema(loadChild((JSONObject) itemSchema));
      } else if (itemSchema instanceof JSONArray) {
        buildTupleSchema(builder, itemSchema);
      } else {
        throw new SchemaException("'items' must be an array or object, found "
            + itemSchema.getClass().getSimpleName());
      }
    }
    return builder.build();
  }

  private CombinedSchema buildCombinedSchema() {
    List<String> presentKeys = COMBINED_SUBSCHEMA_PROVIDERS.keySet().stream()
        .filter(schemaJson::has)
        .collect(Collectors.toList());
    if (presentKeys.size() != 1) {
      throw new SchemaException(String.format(
          "expected exactly 1 of 'allOf', 'anyOf', 'oneOf', %d found", presentKeys.size()));
    }
    String key = presentKeys.get(0);
    JSONArray subschemaDefs = schemaJson.getJSONArray(key);
    Collection<Schema> subschemas = IntStream.range(0, subschemaDefs.length())
        .mapToObj(subschemaDefs::getJSONObject)
        .map(this::loadChild)
        .collect(Collectors.toList());
    return COMBINED_SUBSCHEMA_PROVIDERS.get(key).apply(subschemas);
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

  private ObjectSchema buildObjectSchema() {
    ObjectSchema.Builder builder = ObjectSchema.builder();
    ifPresent("minProperties", Integer.class, builder::minProperties);
    ifPresent("maxProperties", Integer.class, builder::maxProperties);
    if (schemaJson.has("properties")) {
      JSONObject propertyDefs = schemaJson.getJSONObject("properties");
      Arrays.stream(JSONObject.getNames(propertyDefs))
      .forEach(key -> builder.addPropertySchema(key,
          loadChild(propertyDefs.getJSONObject(key))));
    }
    if (schemaJson.has("additionalProperties")) {
      Object addititionalDef = schemaJson.get("additionalProperties");
      if (addititionalDef instanceof Boolean) {
        builder.additionalProperties((Boolean) addititionalDef);
      } else if (addititionalDef instanceof JSONObject) {
        builder.schemaOfAdditionalProperties(loadChild((JSONObject) addititionalDef));
      } else {
        throw new SchemaException(String.format(
            "additionalProperties must be boolean or object, found: [%s]",
            addititionalDef.getClass().getSimpleName()));
      }
    }
    if (schemaJson.has("required")) {
      JSONArray requiredJson = schemaJson.getJSONArray("required");
      IntStream.range(0, requiredJson.length())
      .mapToObj(requiredJson::getString)
      .forEach(builder::addRequiredProperty);
    }
    ifPresent("dependencies", JSONObject.class, deps -> this.addDependencies(builder, deps));
    return builder.build();
  }

  private Schema buildStringSchema() {
    return new StringSchema(getInteger("minLength"), getInteger("maxLength"),
        getString("pattern"));
  }

  private void buildTupleSchema(final ArraySchema.Builder builder, final Object itemSchema) {
    JSONArray itemSchemaJsons = (JSONArray) itemSchema;
    for (int i = 0; i < itemSchemaJsons.length(); ++i) {
      Object itemSchemaJson = itemSchemaJsons.get(i);
      if (!(itemSchemaJson instanceof JSONObject)) {
        throw new SchemaException("array item schema must be an object, found "
            + itemSchemaJson.getClass().getSimpleName());
      }
      builder.addItemSchema(loadChild((JSONObject) itemSchemaJson));
    }
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
    if (!schemaJson.has("type")) {
      if (schemaJson.has("not")) {
        return buildNotSchema();
      } else if (schemaJson.has("$ref")) {
        return lookupReference(schemaJson.getString("$ref"));
      }
      return buildCombinedSchema();
    }
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
      case "object":
        return buildObjectSchema();
      default:
        throw new SchemaException(String.format("unknown type: [%s]", type));
    }
  }

  private Schema lookupReference(final String pointer) {
    String[] path = pointer.split("/");
    if (!"#".equals(path[0])) {
      throw new IllegalArgumentException("JSON pointers must start with a '#'");
    }
    JSONObject current = rootSchemaJson;
    for (int i = 1; i < path.length; ++i) {
      current = current.getJSONObject(path[i]);
    }
    return new SchemaLoader(current, rootSchemaJson).load();
  }

  private NotSchema buildNotSchema() {
    Schema mustNotMatch = loadChild(schemaJson.getJSONObject("not"));
    return new NotSchema(mustNotMatch);
  }
}
