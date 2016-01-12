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
package org.everit.json.schema.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.EmptySchema;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.NotSchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ObjectSchema.Builder;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.loader.internal.ConsumerJ6;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.everit.json.schema.loader.internal.FunctionJ6;
import org.everit.json.schema.loader.internal.JSONPointer;
import org.everit.json.schema.loader.internal.JSONPointer.QueryResult;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Loads a JSON schema's JSON representation into schema validator instances.
 */
public class SchemaLoader {

  /**
   * Created and used by {@link TypeBasedMultiplexer} to set actions (consumers) for matching
   * classes.
   */
  interface OnTypeConsumer<E>
  {
    TypeBasedMultiplexer then(ConsumerJ6<E> consumer);
  }

  /**
   * Used by {@code SchemaLoader} during schema loading for type-based action selections. In other
   * words this utility class is used for avoiding {@code if..instanceof..casting} constructs.
   * Together with the {@link OnTypeConsumer} implementations it forms a fluent API to deal with the
   * parts of the JSON schema where multiple kind of values are valid for a given key.
   *
   * <p>
   * Example usage: <code>
   * Object additProps = schemaJson.get("additionalProperties");
   * typeMultiplexer(additionalProps)
   * .ifIs(JSONArray.class).then(arr -> {...if additProps is a JSONArray then process it... })
   * .ifObject().then(obj -> {...if additProps is a JSONArray then process it... })
   * .requireAny(); // throw a SchemaException if additProps is neither a JSONArray nor a JSONObject
   * </code>
   * </p>
   */
  class TypeBasedMultiplexer {

    /**
     * An {@link OnTypeConsumer} implementation which wraps the action ({@code obj} consumer} set by
     * {@link #then(ConsumerJ6)} into an other consumer which maintains {@link SchemaLoader#id}.
     */
    private class IdModifyingTypeConsumerImpl extends OnTypeConsumerImpl<JSONObject> 
    {

      public IdModifyingTypeConsumerImpl(final Class<?> key) {
        super(key);
      }

      /**
       * Puts the {@code consumer} action with the {@code key} to the {@link TypeBasedMultiplexer}'s
       * action map, and wraps the consumer to an other consumer which properly maintains the
       * {@link SchemaLoader#id} attribute.
       *
       * @see {@link TypeBasedMultiplexer#ifObject()} for more details about the wrapping.
       */
      @Override
      public TypeBasedMultiplexer then(final ConsumerJ6<JSONObject> consumer) 
      {
        ConsumerJ6<JSONObject> wrapperConsumer = new ConsumerJ6<JSONObject>()
		{
			@Override
			public void accept(JSONObject obj) 
			{
			  String origId = id;
	          if (obj.has("id")) {
	              id += obj.getString("id");
	            }
	            consumer.accept(obj);
	            id = origId;
			}
		};
        actions.put(key, wrapperConsumer);
        return TypeBasedMultiplexer.this;
      }

    }

    /**
     * Default implementation of {@link OnTypeConsumer}, instantiated by
     * {@link TypeBasedMultiplexer#ifIs(Class)}.
     */
    private class OnTypeConsumerImpl<E> implements OnTypeConsumer<E> {

      protected final Class<?> key;

      public OnTypeConsumerImpl(final Class<?> key) {
        this.key = key;
      }

      @Override
      public TypeBasedMultiplexer then(final ConsumerJ6<E> consumer) {
        actions.put(key, consumer);
        return TypeBasedMultiplexer.this;
      }

    }

    private final String keyOfObj;

    private final Object obj;

    private final Map<Class<?>, ConsumerJ6<?>> actions = new HashMap<Class<?>, ConsumerJ6<?>>();

    /**
     * Constructor with {@code null} {@code keyOfObj}.
     */
    public TypeBasedMultiplexer(final Object obj) {
      this(null, obj);
    }

    /**
     * Constructor.
     *
     * @param keyOfObj
     *          is an optional (nullable) string used by {@link #requireAny()} to construct the
     *          message of the {@link SchemaException} if no appropriate consumer action is found.
     * @param obj
     *          the object which' class is matched against the classes defined by
     *          {@link #ifIs(Class)} (or {@link #ifObject()}) calls.
     */
    public TypeBasedMultiplexer(final String keyOfObj, final Object obj) {
      this.keyOfObj = keyOfObj;
      this.obj = obj;
    }

    /**
     * Creates a setter which will be invoked by {@link #orElse(ConsumerJ6)} or {@link #requireAny()}
     * if {@code obj} is an instance of {@code predicateClass}.
     *
     * @throws IllegalArgumentException
     *           if {@code predicateClass} is {@link JSONObject}. Use {@link #ifObject()} for
     *           matching {@code obj}'s class against {@link JSONObject}.
     */
    public <E> OnTypeConsumer<E> ifIs(final Class<E> predicateClass) {
      if (predicateClass == JSONObject.class) {
        throw new IllegalArgumentException("use ifObject() instead");
      }
      return new OnTypeConsumerImpl<E>(predicateClass);
    }

    /**
     * Creates a {@link JSONObject} consumer setter.
     *
     * <p>
     * The returned {@link OnTypeConsumer} implementation will wrap the
     * {@link OnTypeConsumer#then(ConsumerJ6) passed consumer action} with an other consumer which
     * properly maintains the {@link SchemaLoader#id} attribute, ie. if {@code obj} is a
     * {@link JSONObject} instance and it has an {@code id} property then it will append this id
     * value to {@link SchemaLoader#id} for the duration of the action execution, then it will
     * restore the original id.
     * </p>
     */
    public OnTypeConsumer<JSONObject> ifObject() {
      return new IdModifyingTypeConsumerImpl(JSONObject.class);
    }

    /**
     * Checks if the {@code obj} is an instance of any previously set classes (by
     * {@link #ifIs(Class)} or {@link #ifObject()}), performs the mapped action of found or invokes
     * {@code orElseConsumer} with the {@code obj}.
     */
    public void orElse(final ConsumerJ6<Object> orElseConsumer)
    {
    	ConsumerJ6<Object> consumer = null;
    	Iterator<Class<?>> iterator = actions.keySet().iterator();
    	
    	while(iterator.hasNext())
    	{
    		Class<?> clazz = iterator.next();
    		if(clazz.isAssignableFrom(obj.getClass()))
    		{
    			consumer = (ConsumerJ6<Object>) actions.get(clazz);
    			consumer.accept(obj);
    			return;
    		}
    	}
    	orElseConsumer.accept(obj);
    }

    /**
     * Checks if the {@code obj} is an instance of any previously set classes (by
     * {@link #ifIs(Class)} or {@link #ifObject()}), performs the mapped action of found or throws
     * with a {@link SchemaException}.
     */
    public void requireAny()
    {
      orElse(new ConsumerJ6<Object>()
      {
		@Override
		public void accept(Object t) 
		{
	        throw new SchemaException(keyOfObj, new ArrayList<Class<?>>(actions.keySet()), obj);
		}
      });
    }
  }

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

  private static final Map<String, FunctionJ6<Collection<Schema>, CombinedSchema.Builder>> //
  COMBINED_SUBSCHEMA_PROVIDERS = new HashMap<String, FunctionJ6<Collection<Schema>, CombinedSchema.Builder>>(3);

  static
  {
    COMBINED_SUBSCHEMA_PROVIDERS.put("allOf", 
		new FunctionJ6<Collection<Schema>, CombinedSchema.Builder>()
		{
			@Override
			public org.everit.json.schema.CombinedSchema.Builder 
			apply(Collection<Schema> schemas) 
			{
				return CombinedSchema.allOf(schemas);				
			}
		});
    COMBINED_SUBSCHEMA_PROVIDERS.put("anyOf", 
		new FunctionJ6<Collection<Schema>, CombinedSchema.Builder>()
		{
			@Override
			public org.everit.json.schema.CombinedSchema.Builder 
			apply(Collection<Schema> schemas) 
			{
				return CombinedSchema.anyOf(schemas);    				
			}
		}); 
    COMBINED_SUBSCHEMA_PROVIDERS.put("oneOf", 
		new FunctionJ6<Collection<Schema>, CombinedSchema.Builder>()
		{
			@Override
			public org.everit.json.schema.CombinedSchema.Builder 
			apply(Collection<Schema> schemas) 
			{
				return CombinedSchema.oneOf(schemas);
				
			}
		});
  }

  /**
   * Loads a JSON schema to a schema validator using a {@link DefaultSchemaClient default HTTP
   * client}.
   *
   * @param schemaJson
   *          the JSON representation of the schema.
   * @return the schema validator object
   */
  public static Schema load(final JSONObject schemaJson) {
    return load(schemaJson, new DefaultSchemaClient());
  }

  /**
   * Creates Schema instance from its JSON representation.
   *
   * @param schemaJson
   *          the JSON representation of the schema.
   * @param httpClient
   *          the HTTP client to be used for resolving remote JSON references.
   * @return the created schema
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
public static Schema load(final JSONObject schemaJson, final SchemaClient httpClient)
  {
    String schemaId = schemaJson.optString("id");
    return new SchemaLoader(schemaId, schemaJson, schemaJson, 
    		(Map)new HashMap<String,Builder>(), httpClient
    		).load().build();
  }

  private String id = null;

  private final JSONObject schemaJson;

  private final JSONObject rootSchemaJson;

  private final SchemaClient httpClient;

  private final Map<String, ReferenceSchema.Builder> pointerSchemas;

  /**
   * Constructor.
   */
  SchemaLoader(final String id, final JSONObject schemaJson,
      final JSONObject rootSchemaJson, final Map<String, ReferenceSchema.Builder> pointerSchemas,
      final SchemaClient httpClient) 
  {
    if((this.schemaJson = schemaJson)==null)
    {
    	throw new NullPointerException("schemaJson cannot be null");
    }
    if((this.rootSchemaJson = rootSchemaJson)==null)
    {
    	throw new NullPointerException("rootSchemaJson cannot be null");
    }
    this.id = id;
    if((this.httpClient = httpClient)==null)
    {
    	throw new NullPointerException("httpClient cannot be null");
    }
    this.pointerSchemas = pointerSchemas;
  }

  private void addDependencies(final Builder builder, final JSONObject deps)
  {
	String[] names = JSONObject.getNames(deps);
	int index= 0;
	int length = names==null?0:names.length;
	for(;index < length;index++)
	{
		addDependency(builder, names[index], deps.get(names[index]));
	}
  }

  private void addDependency(final Builder builder, final String ifPresent, final Object deps) 
  {
    typeMultiplexer(deps).ifObject().then(new ConsumerJ6<JSONObject>()
	{
		@Override
		public void accept(JSONObject obj) 
		{
			builder.schemaDependency(ifPresent, loadChild(obj).build());
			
		}
	}).ifIs(JSONArray.class).then(new ConsumerJ6<JSONArray>()
	{
		@Override
		public void accept(JSONArray obj) 
		{
			int index = 0;
			int length = obj==null?0:obj.length();
			for(;index < length;index++)
			{
				String dependency = obj.getString(index);
				builder.propertyDependency(ifPresent, dependency);
			}			
		}
	}).requireAny();
  }

  private CombinedSchema.Builder buildAnyOfSchemaForMultipleTypes() {
    JSONArray subtypeJsons = schemaJson.getJSONArray("type");
    Map<String, Object> dummyJson = new HashMap<String, Object>();
    Collection<Schema> subschemas = new ArrayList<Schema>(subtypeJsons.length());
    for (int i = 0; i < subtypeJsons.length(); ++i) {
      Object subtypeJson = subtypeJsons.get(i);
      dummyJson.put("type", subtypeJson);
      JSONObject child = new JSONObject(dummyJson);
      subschemas.add(loadChild(child).build());
    }
    return CombinedSchema.anyOf(subschemas);
  }

  private ArraySchema.Builder buildArraySchema() 
  {
    final ArraySchema.Builder builder = ArraySchema.builder();
    ifPresent("minItems", Integer.class, new ConsumerJ6<Integer>(){

		@Override
		public void accept(Integer minItems)
		{
			builder.minItems(minItems);
		}}
    );
    ifPresent("maxItems", Integer.class, new ConsumerJ6<Integer>(){

		@Override
		public void accept(Integer maxItems)
		{
			builder.maxItems(maxItems);
		}}
    );
    ifPresent("uniqueItems", Boolean.class, new ConsumerJ6<Boolean>(){

		@Override
		public void accept(Boolean uniqueItems)
		{
			builder.uniqueItems(uniqueItems);
		}}
    );
    if (schemaJson.has("additionalItems")) 
    {
      typeMultiplexer("additionalItems", schemaJson.get("additionalItems"))
          .ifIs(Boolean.class).then(new ConsumerJ6<Boolean>()
		  {

			@Override
			public void accept(Boolean additionalItems) 
			{
				builder.additionalItems(additionalItems);
			}
		  }).ifObject().then(new ConsumerJ6<JSONObject>()
		  {
			@Override
			public void accept(JSONObject jsonObj) 
			{
				builder.schemaOfAdditionalItems(
						loadChild(jsonObj).build());
			}
		  }).requireAny();
    }
    if (schemaJson.has("items")) {
      typeMultiplexer("items", schemaJson.get("items"))
          .ifObject().then(new ConsumerJ6<JSONObject>()
		  {
			@Override
			public void accept(JSONObject itemSchema) 
			{
				builder.allItemSchema(loadChild(itemSchema).build());
				
			}
		  }).ifIs(JSONArray.class).then(new ConsumerJ6<JSONArray>()
	        {
				@Override
				public void accept(JSONArray arr) 
				{
					buildTupleSchema(builder, arr);
					
				}
			}).requireAny();
    }
    return builder;
  }

  private EnumSchema.Builder buildEnumSchema() 
  {
    Set<Object> possibleValues = new HashSet<Object>();
    JSONArray arr = schemaJson.getJSONArray("enum");
    int index = 0;
    int length = arr==null?0:arr.length();
    for(;index < length;index++)
    {
    	possibleValues.add(arr.get(index));
    }
    return EnumSchema.builder().possibleValues(possibleValues);
  }

  private NotSchema.Builder buildNotSchema()
  {
    Schema mustNotMatch = loadChild(schemaJson.getJSONObject("not")).build();
    return NotSchema.builder().mustNotMatch(mustNotMatch);
  }

  private NumberSchema.Builder buildNumberSchema() 
  {    
	final NumberSchema.Builder builder = NumberSchema.builder();
    ifPresent("minimum", Number.class, new ConsumerJ6<Number>(){

		@Override
		public void accept(Number minimum)
		{
			builder.minimum(minimum);
		}}
    );
    ifPresent("maximum", Number.class,  new ConsumerJ6<Number>(){

		@Override
		public void accept(Number maximum)
		{
			builder.maximum(maximum);
		}}
    );
    ifPresent("multipleOf", Number.class,  new ConsumerJ6<Number>(){

		@Override
		public void accept(Number multipleOf)
		{
			builder.multipleOf(multipleOf);
		}}
    );
    ifPresent("exclusiveMinimum", Boolean.class, new ConsumerJ6<Boolean>(){

		@Override
		public void accept(Boolean exclusiveMinimum)
		{
			builder.exclusiveMinimum(exclusiveMinimum);
		}}
    );
    ifPresent("exclusiveMaximum", Boolean.class,  new ConsumerJ6<Boolean>(){

		@Override
		public void accept(Boolean exclusiveMaximum)
		{
			builder.exclusiveMaximum(exclusiveMaximum);
		}}
    );
    return builder;
  }

  private ObjectSchema.Builder buildObjectSchema()
  {
    final ObjectSchema.Builder builder = ObjectSchema.builder();
    ifPresent("minProperties", Integer.class, new ConsumerJ6<Integer>(){

		@Override
		public void accept(Integer minProperties)
		{
			builder.minProperties(minProperties);
		}}
    );
    ifPresent("maxProperties", Integer.class, new ConsumerJ6<Integer>(){

		@Override
		public void accept(Integer maxProperties)
		{
			builder.maxProperties(maxProperties);
		}}
    );
    if (schemaJson.has("properties")) 
    {
      JSONObject propertyDefs = schemaJson.getJSONObject("properties");
      if(propertyDefs != null)
      {
    	 String[] names = JSONObject.getNames(propertyDefs);
    	 int index = 0;
    	 int length = names==null?0:names.length;
    	 for(;index < length;index++)
    	 {
    		 String key = names[index];
    		 builder.addPropertySchema(key,
              loadChild(propertyDefs.getJSONObject(key)).build());
    	 }
      }
    }
    if (schemaJson.has("additionalProperties"))
    {
      typeMultiplexer("additionalProperties", schemaJson.get("additionalProperties")
      ).ifIs(Boolean.class).then(new ConsumerJ6<Boolean>()
		{
			@Override
			public void accept(Boolean additionalProperties) 
			{
				builder.additionalProperties(additionalProperties);						
			}	  
		}).ifObject().then(new ConsumerJ6<JSONObject>()
		{
			@Override
			public void accept(JSONObject def) 
			{
				builder.schemaOfAdditionalProperties(loadChild(def).build());						
			}	  
		 }).requireAny();
    }
    if (schemaJson.has("required")) 
    {
      JSONArray requiredJson = schemaJson.getJSONArray("required");
      int index= 0;
      int length = requiredJson==null?0:requiredJson.length();
      
      for(;index < length; index++)
      {
    	  builder.addRequiredProperty(
    			  requiredJson.getString(index));
      }
    }
    if (schemaJson.has("patternProperties")) 
    {
      JSONObject patternPropsJson = schemaJson.getJSONObject("patternProperties");
      String[] patterns = JSONObject.getNames(patternPropsJson);
      if (patterns != null) 
      {
        for (String pattern : patterns)
        {
          builder.patternProperty(pattern, loadChild(patternPropsJson.getJSONObject(pattern))
              .build());
        }
      }
    }
    ifPresent("dependencies", JSONObject.class, new ConsumerJ6<JSONObject>()
    {
		@Override
		public void accept(JSONObject deps)
		{
			SchemaLoader.this.addDependencies(builder, deps);
		}}
    );
    return builder;
  }

  private Schema.Builder<?> buildSchemaWithoutExplicitType() {
    if (schemaJson.length() == 0) {
      return EmptySchema.builder();
    }
    if (schemaJson.has("$ref")) {
      return lookupReference(schemaJson.getString("$ref"));
    }
    Schema.Builder<?> rval = sniffSchemaByProps();
    if (rval != null) {
      return rval;
    }
    if (schemaJson.has("not")) {
      return buildNotSchema();
    }
    return EmptySchema.builder();
  }

  private StringSchema.Builder buildStringSchema() 
  {
    final StringSchema.Builder builder = StringSchema.builder();
    ifPresent("minLength", Integer.class, new ConsumerJ6<Integer>(){

		@Override
		public void accept(Integer minLength)
		{
			builder.minLength(minLength);
		}}
    );
    ifPresent("maxLength", Integer.class, new ConsumerJ6<Integer>(){

		@Override
		public void accept(Integer maxLength)
		{
			builder.maxLength(maxLength);
		}}
    );
    ifPresent("pattern", String.class, new ConsumerJ6<String>(){

		@Override
		public void accept(String pattern)
		{
			builder.pattern(pattern);
		}});
    return builder;
  }

  private void buildTupleSchema(final ArraySchema.Builder builder, 
		  final JSONArray itemSchema) 
  {
    for (int i = 0; i < itemSchema.length(); ++i)
    {
      typeMultiplexer(itemSchema.get(i)).ifObject().then(
      new ConsumerJ6<JSONObject>()
      {
		@Override
		public void accept(JSONObject schema) 
		{			
			builder.addItemSchema(loadChild(schema).build());
		}        	  
      }).requireAny();
    }
  }

  private <E> void ifPresent(final String key, final Class<E> expectedType, 
		  final ConsumerJ6<E> consumer)
  {
    if (schemaJson.has(key))
    {
      @SuppressWarnings("unchecked")
      E value = (E) schemaJson.get(key);
      try 
      {
        consumer.accept(value);
        
      } catch (ClassCastException e)
      {
        throw new SchemaException(key, expectedType, value);
      }
    }
  }

  /**
   * Populates a {@code Schema.Builder} instance from the {@code schemaJson} schema definition.
   *
   * @return the builder which already contains the validation criteria of the schema, therefore
   *         {@link Schema.Builder#build()} can be immediately used to acquire the {@link Schema}
   *         instance to be used for validation
   */
  private Schema.Builder<?> load() {
    Schema.Builder<?> builder;
    
    if (schemaJson.has("enum"))
    {
      builder = buildEnumSchema();
      
    } else 
    {
      builder = tryCombinedSchema();
      if (builder == null) {
        if (!schemaJson.has("type")) {
          builder = buildSchemaWithoutExplicitType();
        } else {
          builder = loadForType(schemaJson.get("type"));
        }
      }
    }
    final org.everit.json.schema.Schema.Builder<?> finalBuilder = builder;
    ifPresent("id", String.class, new ConsumerJ6<String>()
	{
		@Override
		public void accept(String id) 
		{					
			finalBuilder.id(id);
		}    	
	});
    ifPresent("title", String.class, new ConsumerJ6<String>()
	{
		@Override
		public void accept(String title) 
		{					
			finalBuilder.title(title);
		}    	
	});
    ifPresent("description", String.class,new ConsumerJ6<String>()
	{
		@Override
		public void accept(String description) 
		{					
			finalBuilder.description(description);
		}    	
	});
    return builder;
  }

private Schema.Builder<?> loadChild(final JSONObject childJson) {
    return new SchemaLoader(id, childJson, rootSchemaJson, pointerSchemas,
        httpClient).load();
  }

  private Schema.Builder<?> loadForExplicitType(final String typeString) {

      if("string".equals(typeString))
      {
        return buildStringSchema();
      }
      if("integer".equals(typeString))
      {
        return buildNumberSchema().requiresInteger(true);
      }
      if("number".equals(typeString))
      {
        return buildNumberSchema();
      }
      if("boolean".equals(typeString))
      {
        return BooleanSchema.builder();
      }
      if("null".equals(typeString))
      {
        return NullSchema.builder();
      }
      if("array".equals(typeString))
      {
        return  buildArraySchema();
      }
      if("object".equals(typeString))
      {
        return buildObjectSchema();
      }
      throw new SchemaException(String.format("unknown type: [%s]", typeString));
  }

  private Schema.Builder<?> loadForType(final Object type) {
    if (type instanceof JSONArray) {
      return buildAnyOfSchemaForMultipleTypes();
    } else if (type instanceof String) {
      return loadForExplicitType((String) type);
    } else {
      throw new SchemaException("type", Arrays.asList(JSONArray.class, String.class), type);
    }
  }

  /**
   * Returns a schema builder instance after looking up the JSON pointer.
   */
  private Schema.Builder<?> lookupReference(final String relPointerString) {
    String absPointerString = id + relPointerString;
    if (pointerSchemas.containsKey(absPointerString)) {
      return pointerSchemas.get(absPointerString);
    }
    JSONPointer pointer;
    if (absPointerString.startsWith("#")) {
      pointer = JSONPointer.forDocument(rootSchemaJson, absPointerString);
    } else {
      pointer = JSONPointer.forURL(httpClient, absPointerString);
    }
    ReferenceSchema.Builder refBuilder = ReferenceSchema.builder();
    pointerSchemas.put(absPointerString, refBuilder);
    QueryResult result = pointer.query();
    SchemaLoader childLoader = new SchemaLoader(id, result.getQueryResult(),
        result.getContainingDocument(), pointerSchemas, httpClient);
    Schema referredSchema = childLoader.load().build();
    refBuilder.build().setReferredSchema(referredSchema);
    return refBuilder;
  }

  private boolean schemaHasAnyOf(final Collection<String> propNames) 
  {
	  Iterator<String> iterator = propNames.iterator();
	  while(iterator.hasNext())
	  {
		  String key = iterator.next();
		  if(schemaJson.has(key))
		  {
			  return true;
		  }
	  }
	  return false;
  }

  private Schema.Builder<?> sniffSchemaByProps() {
    if (schemaHasAnyOf(ARRAY_SCHEMA_PROPS)) {
      return buildArraySchema().requiresArray(false);
    } else if (schemaHasAnyOf(OBJECT_SCHEMA_PROPS)) {
      return buildObjectSchema().requiresObject(false);
    } else if (schemaHasAnyOf(NUMBER_SCHEMA_PROPS)) {
      return buildNumberSchema().requiresNumber(false);
    } else if (schemaHasAnyOf(STRING_SCHEMA_PROPS)) {
      return buildStringSchema().requiresString(false);
    }
    return null;
  }

  private CombinedSchema.Builder tryCombinedSchema()
  {
	List<String> presentKeys = new ArrayList<String>();
	  
	Iterator<String> iterator = COMBINED_SUBSCHEMA_PROVIDERS.keySet().iterator();
    while(iterator.hasNext())
    {
    	String presentKey = iterator.next();
    	if( schemaJson.has(presentKey))
    	{
    		presentKeys.add(presentKey);
    	}
      }    
    if (presentKeys.size() > 1) 
    {
      throw new SchemaException(String.format(
          "expected at most 1 of 'allOf', 'anyOf', 'oneOf', %d found", presentKeys.size()));
      
    } else if (presentKeys.size() == 1)
    {
      String key = presentKeys.get(0);
      Collection<Schema> subschemas = new ArrayList<Schema>();
      
      JSONArray subschemaDefs = schemaJson.getJSONArray(key);
      int index = 0;
      int length = subschemaDefs==null?0:subschemaDefs.length();
      
      for(;index < length;index++)
      {
    	  JSONObject childJson = subschemaDefs.getJSONObject(index);
    	  subschemas.add(
    		this.loadChild(childJson).build());
      }
      CombinedSchema.Builder combinedSchema = COMBINED_SUBSCHEMA_PROVIDERS.get(key).apply(
          subschemas);
      Schema.Builder<?> baseSchema;
      if (schemaJson.has("type")) {
        baseSchema = loadForType(schemaJson.get("type"));
      } else {
        baseSchema = sniffSchemaByProps();
      }
      if (baseSchema == null) {
        return combinedSchema;
      } else {
        return CombinedSchema.allOf(Arrays.asList(baseSchema.build(), combinedSchema.build()));
      }
    } else 
    {
      return null;
    }
  }

  TypeBasedMultiplexer typeMultiplexer(final Object obj) {
    return new TypeBasedMultiplexer(obj);
  }

  TypeBasedMultiplexer typeMultiplexer(final String keyOfObj, final Object obj) {
    return new TypeBasedMultiplexer(keyOfObj, obj);
  }
}
