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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.everit.jsonvalidator.ArraySchema;
import org.everit.jsonvalidator.BooleanSchema;
import org.everit.jsonvalidator.CombinedSchema;
import org.everit.jsonvalidator.EmptySchema;
import org.everit.jsonvalidator.EnumSchema;
import org.everit.jsonvalidator.NotSchema;
import org.everit.jsonvalidator.NullSchema;
import org.everit.jsonvalidator.NumberSchema;
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

  private static final List<String> ARRAY_SCHEMA_PROPS = Arrays.asList("items", "additionalItems",
      "minItems",
      "maxItems",
      "uniqueItems");

  private static final List<String> OBJECT_SCHEMA_PROPS = Arrays.asList("properties", "required",
      "minProperties",
      "maxProperties",
      "dependencies",
      "patternProperties");

  private static final List<String> INTEGER_SCHEMA_PROPS = Arrays.asList("minimum", "maximum",
      "minimumExclusive", "maximumExclusive", "multipleOf");

  private static final List<String> STRING_SCHEMA_PROPS = Arrays.asList("minLength", "maxLength",
      "pattern");

  private static final Map<String, Function<Collection<Schema>, CombinedSchema>> //
  COMBINED_SUBSCHEMA_PROVIDERS = new HashMap<>(3);

  static {
    COMBINED_SUBSCHEMA_PROVIDERS.put("allOf", CombinedSchema::allOf);
    COMBINED_SUBSCHEMA_PROVIDERS.put("anyOf", CombinedSchema::anyOf);
    COMBINED_SUBSCHEMA_PROVIDERS.put("oneOf", CombinedSchema::oneOf);
  }

  public static Schema load(final JSONObject schemaJson) {
    return new SchemaLoader(schemaJson, schemaJson).load();
  }

  private final JSONObject schemaJson;

  private final JSONObject rootSchemaJson;

  public SchemaLoader(final JSONObject schemaJson, final JSONObject rootSchemaJson) {
    this.schemaJson = Objects.requireNonNull(schemaJson, "schemaJson cannot be null");
    this.rootSchemaJson = Objects.requireNonNull(rootSchemaJson, "rootSchemaJson cannot be null");
  }

  private void addDependencies(final Builder builder, final JSONObject deps) {
    Arrays.stream(JSONObject.getNames(deps))
    .forEach(ifPresent -> addDependency(builder, ifPresent, deps.get(ifPresent)));
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

  private CombinedSchema buildAnyOfSchemaForMultipleTypes() {
    JSONArray subtypeJsons = schemaJson.getJSONArray("type");
    Map<String, Object> dummyJson = new HashMap<String, Object>();
    Collection<Schema> subschemas = new ArrayList<Schema>(subtypeJsons.length());
    for (int i = 0; i < subtypeJsons.length(); ++i) {
      Object subtypeJson = subtypeJsons.get(i);
      dummyJson.put("type", subtypeJson);
      JSONObject child = new JSONObject(dummyJson);
      subschemas.add(loadChild(child));
    }
    return CombinedSchema.anyOf(subschemas);
  }

  private ArraySchema.Builder buildArraySchema() {
    ArraySchema.Builder builder = ArraySchema.builder();
    ifPresent("minItems", Integer.class, builder::minItems);
    ifPresent("maxItems", Integer.class, builder::maxItems);
    ifPresent("uniqueItems", Boolean.class, builder::uniqueItems);
    if (schemaJson.has("additionalItems")) {
      Object additionalItems = schemaJson.get("additionalItems");
      if (additionalItems instanceof Boolean) {
        builder.additionalItems((Boolean) additionalItems);
      } else if (additionalItems instanceof JSONObject) {
        builder.schemaOfAdditionalItems(loadChild((JSONObject) additionalItems));
      } else {
        throw new SchemaException("additionalItems",
            Arrays.asList(Boolean.class, JSONObject.class), additionalItems);
      }
    }
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
    return builder;
  }

  private NotSchema buildNotSchema() {
    Schema mustNotMatch = loadChild(schemaJson.getJSONObject("not"));
    return new NotSchema(mustNotMatch);
  }

  private NumberSchema.Builder buildNumberSchema() {
    NumberSchema.Builder builder = NumberSchema.builder();
    ifPresent("minimum", Number.class, builder::minimum);
    ifPresent("maximum", Number.class, builder::maximum);
    ifPresent("multipleOf", Number.class, builder::multipleOf);
    ifPresent("exclusiveMinimum", Boolean.class, builder::exclusiveMinimum);
    ifPresent("exclusiveMaximum", Boolean.class, builder::exclusiveMaximum);
    return builder;
  }

  private ObjectSchema.Builder buildObjectSchema() {
    ObjectSchema.Builder builder = ObjectSchema.builder();
    ifPresent("minProperties", Integer.class, builder::minProperties);
    ifPresent("maxProperties", Integer.class, builder::maxProperties);
    if (schemaJson.has("properties")) {
      JSONObject propertyDefs = schemaJson.getJSONObject("properties");
      Arrays.stream(Optional.ofNullable(JSONObject.getNames(propertyDefs)).orElse(new String[0]))
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
    if (schemaJson.has("patternProperties")) {
      JSONObject patternPropsJson = schemaJson.getJSONObject("patternProperties");
      String[] patterns = JSONObject.getNames(patternPropsJson);
      if (patterns != null) {
        for (String pattern : patterns) {
          builder.patternProperty(pattern, loadChild(patternPropsJson.getJSONObject(pattern)));
        }
      }
    }
    ifPresent("dependencies", JSONObject.class, deps -> this.addDependencies(builder, deps));
    return builder;
  }

  private Schema buildSchemaWithoutExplicitType() {
    if (schemaJson.length() == 0) {
      return EmptySchema.INSTANCE;
    }
    Schema rval = sniffSchemaByProps();
    if (rval != null) {
      return rval;
    }
    if (schemaJson.has("not")) {
      return buildNotSchema();
    } else if (schemaJson.has("$ref")) {
      return lookupReference(schemaJson.getString("$ref"));
    }
    throw new IllegalArgumentException("failed to build schema for " + schemaJson);
  }

  private StringSchema.Builder buildStringSchema() {
    StringSchema.Builder builder = StringSchema.builder();
    ifPresent("minLength", Integer.class, builder::minLength);
    ifPresent("maxLength", Integer.class, builder::maxLength);
    ifPresent("pattern", String.class, builder::pattern);
    return builder;
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

  /**
   * Populates a {@code Schema} instance from the {@code schemaJson} schema definition.
   */
  public Schema load() {
    if (schemaJson.has("enum")) {
      return buildEnumSchema();
    }
    Schema rval = tryCombinedSchema();
    if (rval != null) {
      return rval;
    }
    if (!schemaJson.has("type")) {
      return buildSchemaWithoutExplicitType();
    }
    Object type = schemaJson.get("type");
    if (type instanceof JSONArray) {
      return buildAnyOfSchemaForMultipleTypes();
    } else if (type instanceof String) {
      return loadForExplicitType((String) type);
    } else {
      throw new SchemaException("type", Arrays.asList(JSONArray.class, String.class), type);
    }
  }

  private EnumSchema buildEnumSchema() {
    Set<Object> possibleValues = new HashSet<>();
    JSONArray arr = schemaJson.getJSONArray("enum");
    for (int i = 0; i < arr.length(); ++i) {
      possibleValues.add(arr.get(i));
    }
    return new EnumSchema(possibleValues);
  }

  private Schema loadChild(final JSONObject childJson) {
    return new SchemaLoader(childJson, rootSchemaJson).load();
  }

  private Schema loadForExplicitType(final String typeString) {
    switch (typeString) {
      case "string":
        return buildStringSchema().build();
      case "integer":
        return buildNumberSchema().requiresInteger(true).build();
      case "number":
        return buildNumberSchema().build();
      case "boolean":
        return BooleanSchema.INSTANCE;
      case "null":
        return NullSchema.INSTANCE;
      case "array":
        return buildArraySchema().build();
      case "object":
        return buildObjectSchema().build();
      default:
        throw new SchemaException(String.format("unknown type: [%s]", typeString));
    }
  }

  private Schema lookupReference(final String pointer) {
    String[] path = pointer.split("/");
    if (!"#".equals(path[0])) {
      throw new IllegalArgumentException("JSON pointers must start with a '#'");
    }
    JSONObject current = rootSchemaJson;
    for (int i = 1; i < path.length; ++i) {
      String segment = path[i];
      if (!current.has(segment)) {
        throw new SchemaException(String.format(
            "failed to resolve JSON pointer [%s]. Segment [%s] not found", pointer, segment));
      }
      current = current.getJSONObject(segment);
    }
    return new SchemaLoader(current, rootSchemaJson).load();
  }

  private boolean schemaHasAnyOf(final Collection<String> propNames) {
    return propNames.stream().filter(schemaJson::has).findAny().isPresent();
  }

  private Schema sniffSchemaByProps() {
    if (schemaHasAnyOf(ARRAY_SCHEMA_PROPS)) {
      return buildArraySchema().requiresArray(false).build();
    } else if (schemaHasAnyOf(OBJECT_SCHEMA_PROPS)) {
      return buildObjectSchema().requiresObject(false).build();
    } else if (schemaHasAnyOf(INTEGER_SCHEMA_PROPS)) {
      return buildNumberSchema().requiresNumber(false).build();
    } else if (schemaHasAnyOf(STRING_SCHEMA_PROPS)) {
      return buildStringSchema().requiresString(false).build();
    }
    return null;
  }

  private CombinedSchema tryCombinedSchema() {
    List<String> presentKeys = COMBINED_SUBSCHEMA_PROVIDERS.keySet().stream()
        .filter(schemaJson::has)
        .collect(Collectors.toList());
    if (presentKeys.size() > 1) {
      throw new SchemaException(String.format(
          "expected at most 1 of 'allOf', 'anyOf', 'oneOf', %d found", presentKeys.size()));
    } else if (presentKeys.size() == 1) {
      String key = presentKeys.get(0);
      JSONArray subschemaDefs = schemaJson.getJSONArray(key);
      Collection<Schema> subschemas = IntStream.range(0, subschemaDefs.length())
          .mapToObj(subschemaDefs::getJSONObject)
          .map(this::loadChild)
          .collect(Collectors.toList());
      return COMBINED_SUBSCHEMA_PROVIDERS.get(key).apply(subschemas);
    } else {
      return null;
    }
  }
}
