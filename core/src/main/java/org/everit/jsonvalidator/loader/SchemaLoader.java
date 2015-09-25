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

import org.apache.http.impl.client.HttpClients;
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
import org.everit.jsonvalidator.loader.JSONPointer.QueryResult;
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
      "patternProperties",
      "additionalProperties");

  private static final List<String> NUMBER_SCHEMA_PROPS = Arrays.asList("minimum", "maximum",
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

  @FunctionalInterface
  interface OnTypeConsumer<E> {
    TypeBasedMultiplexer then(Consumer<E> consumer);
  }

  class TypeBasedMultiplexer {

    private class OnTypeConsumerImpl<E> implements OnTypeConsumer<E> {

      private final Class<?> key;

      public OnTypeConsumerImpl(final Class<?> key) {
        this.key = key;
      }

      @Override
      public TypeBasedMultiplexer then(final Consumer<E> consumer) {
        actions.put(key, consumer);
        return TypeBasedMultiplexer.this;
      }

    }

    private final String keyOfObj;

    private final Object obj;

    private final Map<Class<?>, Consumer<?>> actions = new HashMap<>();

    public TypeBasedMultiplexer(final String keyOfObj, final Object obj) {
      this.keyOfObj = keyOfObj;
      this.obj = obj;
    }

    public TypeBasedMultiplexer(final Object obj) {
      this(null, obj);
    }

    public <E> OnTypeConsumer<E> ifIs(final Class<E> predicateClass) {
      return new OnTypeConsumerImpl<E>(predicateClass);
    }

    public void orElse(final Consumer<Object> orElseConsumer) {
      @SuppressWarnings("unchecked")
      Consumer<Object> consumer = (Consumer<Object>) actions.keySet().stream()
      .filter(clazz -> clazz.isAssignableFrom(obj.getClass()))
      .findFirst()
      .map(actions::get)
      .orElse(orElseConsumer::accept);
      consumer.accept(obj);

    }

    public void requireAny() {
      orElse(obj -> {
        throw new SchemaException(keyOfObj, new ArrayList<Class<?>>(actions.keySet()), obj);
      });
    }
  }

  TypeBasedMultiplexer typeMultiplexer(final Object obj) {
    return new TypeBasedMultiplexer(obj);
  }

  TypeBasedMultiplexer typeMultiplexer(final String keyOfObj, final Object obj) {
    return new TypeBasedMultiplexer(keyOfObj, obj);
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

  private void addDependency(final Builder builder, final String ifPresent, final Object deps) {
    typeMultiplexer(deps)
    .ifIs(JSONObject.class).then(obj -> {
      builder.schemaDependency(ifPresent, loadChild(obj));
    })
    .ifIs(JSONArray.class).then(propNames -> {
      IntStream.range(0, propNames.length())
              .mapToObj(i -> propNames.getString(i))
              .forEach(dependency -> builder.propertyDependency(ifPresent, dependency));
    }).requireAny();
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
      typeMultiplexer("additionalItems", schemaJson.get("additionalItems"))
          .ifIs(Boolean.class).then(builder::additionalItems)
          .ifIs(JSONObject.class)
          .then(jsonObj -> builder.schemaOfAdditionalItems(loadChild(jsonObj)))
          .requireAny();
    }
    if (schemaJson.has("items")) {
      typeMultiplexer("items", schemaJson.get("items"))
      .ifIs(JSONObject.class).then(itemSchema -> builder.allItemSchema(loadChild(itemSchema)))
      .ifIs(JSONArray.class).then(arr -> buildTupleSchema(builder, arr))
      .requireAny();
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
      typeMultiplexer("additionalProperties", schemaJson.get("additionalProperties"))
          .ifIs(Boolean.class).then(builder::additionalProperties)
          .ifIs(JSONObject.class).then(def -> builder.schemaOfAdditionalProperties(loadChild(def)))
          .requireAny();
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
    if (schemaJson.has("$ref")) {
      return lookupReference(schemaJson.getString("$ref"));
    }
    Schema rval = sniffSchemaByProps();
    if (rval != null) {
      return rval;
    }
    if (schemaJson.has("not")) {
      return buildNotSchema();
    }
    return EmptySchema.INSTANCE;
  }

  private StringSchema.Builder buildStringSchema() {
    StringSchema.Builder builder = StringSchema.builder();
    ifPresent("minLength", Integer.class, builder::minLength);
    ifPresent("maxLength", Integer.class, builder::maxLength);
    ifPresent("pattern", String.class, builder::pattern);
    return builder;
  }

  private void buildTupleSchema(final ArraySchema.Builder builder, final JSONArray itemSchema) {
    for (int i = 0; i < itemSchema.length(); ++i) {
      typeMultiplexer(itemSchema.get(i))
          .ifIs(JSONObject.class).then(schema -> builder.addItemSchema(loadChild(schema)))
          .requireAny();
    }
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
    return loadForType(schemaJson.get("type"));
  }

  private Schema loadForType(final Object type) {
    // typeMultiplexer(type)
    // .ifIs(JSONArray.class).then(a -> buildAnyOfSchemaForMultipleTypes())
    // .ifIs(String.class).then(this::loadForExplicitType)
    // .requireAny();
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
    IntStream.range(0, arr.length())
        .mapToObj(arr::get)
        .forEach(possibleValues::add);
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

  private Schema lookupReference(final String pointerString) {
    if (pointerString.equals("#")) {
      throw new UnsupportedOperationException("recursive reference");
    }
    JSONPointer pointer;
    if (pointerString.startsWith("#")) {
      pointer = JSONPointer.forDocument(rootSchemaJson, pointerString);
    } else {
      pointer = JSONPointer.forURL(HttpClients.createDefault(), pointerString);
    }
    QueryResult result = pointer.query();
    return new SchemaLoader(result.getQueryResult(), result.getContainingDocument()).load();
  }

  private boolean schemaHasAnyOf(final Collection<String> propNames) {
    return propNames.stream().filter(schemaJson::has).findAny().isPresent();
  }

  private Schema sniffSchemaByProps() {
    if (schemaHasAnyOf(ARRAY_SCHEMA_PROPS)) {
      return buildArraySchema().requiresArray(false).build();
    } else if (schemaHasAnyOf(OBJECT_SCHEMA_PROPS)) {
      return buildObjectSchema().requiresObject(false).build();
    } else if (schemaHasAnyOf(NUMBER_SCHEMA_PROPS)) {
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
      CombinedSchema combinedSchema = COMBINED_SUBSCHEMA_PROVIDERS.get(key).apply(subschemas);
      Schema baseSchema;
      if (schemaJson.has("type")) {
        baseSchema = loadForType(schemaJson.get("type"));
      } else {
        baseSchema = sniffSchemaByProps();
      }
      if (baseSchema == null) {
        return combinedSchema;
      } else {
        return CombinedSchema.allOf(Arrays.asList(baseSchema, combinedSchema));
      }
    } else {
      return null;
    }
  }
}
