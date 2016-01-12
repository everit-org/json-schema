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
package org.everit.json.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.JSONObject;

/**
 * Object schema validator.
 */
public class ObjectSchema extends Schema {

  /**
   * Builder class for {@link ObjectSchema}.
   */
  public static class Builder extends Schema.Builder<ObjectSchema> {

    private final Map<Pattern, Schema> patternProperties = new HashMap<Pattern, Schema>();

    private boolean requiresObject = true;

    private final Map<String, Schema> propertySchemas = new HashMap<String, Schema>();

    private boolean additionalProperties = true;

    private Schema schemaOfAdditionalProperties;

    private final List<String> requiredProperties = new ArrayList<String>(0);

    private Integer minProperties;

    private Integer maxProperties;

    private final Map<String, Set<String>> propertyDependencies = new HashMap<String, Set<String>>();

    private final Map<String, Schema> schemaDependencies = new HashMap<String, Schema>();

    public Builder requiresObject(final boolean requiresObject) {
      this.requiresObject = requiresObject;
      return this;
    }

    public Builder additionalProperties(final boolean additionalProperties) {
      this.additionalProperties = additionalProperties;
      return this;
    }

    public Builder patternProperty(final Pattern pattern, final Schema schema) {
      this.patternProperties.put(pattern, schema);
      return this;
    }

    public Builder patternProperty(final String pattern, final Schema schema) {
      return patternProperty(Pattern.compile(pattern), schema);
    }

    /**
     * Adds a property schema.
     *
     * @param propName
     *          the name of the property which' expected schema must be {@code schema}
     * @param schema
     *          if the subject under validation has a property named {@code propertyName} then its
     *          value will be validated using this {@code schema}
     * @return {@code this}
     */
    public Builder addPropertySchema(final String propName, final Schema schema) 
    {
		if(propName == null)
		{
			throw new NullPointerException("propName cannot be null");
		}
		if(schema == null)
		{
			throw new NullPointerException("schema cannot be null");
		}
		propertySchemas.put(propName, schema);
		return this;
    }

    public Builder addRequiredProperty(final String propertyName) {
      requiredProperties.add(propertyName);
      return this;
    }

    @Override
    public ObjectSchema build() {
      return new ObjectSchema(this);
    }

    public Builder maxProperties(final Integer maxProperties) {
      this.maxProperties = maxProperties;
      return this;
    }

    public Builder minProperties(final Integer minProperties) {
      this.minProperties = minProperties;
      return this;
    }

    /**
     * Adds a property dependency.
     *
     * @param ifPresent
     *          the name of the property which if is present then a property with name
     *          {@code mustBePresent} is mandatory
     * @param mustBePresent
     *          a property with this name must exist in the subject under validation if a property
     *          named {@code ifPresent} exists
     * @return {@code this}
     */
    public Builder propertyDependency(final String ifPresent, final String mustBePresent) {
      Set<String> dependencies = propertyDependencies.get(ifPresent);
      if (dependencies == null) {
        dependencies = new HashSet<String>(1);
        propertyDependencies.put(ifPresent, dependencies);
      }
      dependencies.add(mustBePresent);
      return this;
    }

    public Builder schemaDependency(final String ifPresent, final Schema expectedSchema) {
      schemaDependencies.put(ifPresent, expectedSchema);
      return this;
    }

    public Builder schemaOfAdditionalProperties(final Schema schemaOfAdditionalProperties) {
      this.schemaOfAdditionalProperties = schemaOfAdditionalProperties;
      return this;
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  private final Map<String, Schema> propertySchemas;

  private final boolean additionalProperties;

  private final Schema schemaOfAdditionalProperties;

  private final List<String> requiredProperties;

  private final Integer minProperties;

  private final Integer maxProperties;

  private final Map<String, Set<String>> propertyDependencies;

  private final Map<String, Schema> schemaDependencies;

  private final boolean requiresObject;

  private final Map<Pattern, Schema> patternProperties;

  /**
   * Constructor.
   *
   * @param builder
   *          the builder object containing validation criteria
   */
  public ObjectSchema(final Builder builder) {
    super(builder);
    this.propertySchemas = builder.propertySchemas == null ? null :
        Collections.unmodifiableMap(builder.propertySchemas);
    this.additionalProperties = builder.additionalProperties;
    this.schemaOfAdditionalProperties = builder.schemaOfAdditionalProperties;
    if (!additionalProperties && schemaOfAdditionalProperties != null) {
      throw new SchemaException(
          "additionalProperties cannot be false if schemaOfAdditionalProperties is present");
    }
    this.requiredProperties = Collections.unmodifiableList(new ArrayList<String>(
        builder.requiredProperties));
    this.minProperties = builder.minProperties;
    this.maxProperties = builder.maxProperties;
    this.propertyDependencies = Collections.unmodifiableMap(builder.propertyDependencies);
    this.schemaDependencies = Collections.unmodifiableMap(builder.schemaDependencies);
    this.requiresObject = builder.requiresObject;
    this.patternProperties = Collections.unmodifiableMap(builder.patternProperties);
  }

  private void failure(final String exceptionMessage, final Object... params) {
    throw new ValidationException(String.format(exceptionMessage, params));
  }

  public Integer getMaxProperties() {
    return maxProperties;
  }

  public Integer getMinProperties() {
    return minProperties;
  }

  public Map<String, Set<String>> getPropertyDependencies() {
    return propertyDependencies;
  }

  public Map<String, Schema> getPropertySchemas() {
    return propertySchemas;
  }

  public List<String> getRequiredProperties() {
    return requiredProperties;
  }

  public Map<String, Schema> getSchemaDependencies() {
    return schemaDependencies;
  }

  public Schema getSchemaOfAdditionalProperties() {
    return schemaOfAdditionalProperties;
  }

  public boolean permitsAdditionalProperties() {
    return additionalProperties;
  }

  private boolean matchesAnyPattern(final String key) 
  {
	  Iterator<Pattern> iterator =  patternProperties.keySet().iterator();
	  while(iterator.hasNext())
	  {
		  Pattern pattern = iterator.next();
		  if(pattern.matcher(key).find())
		  {
			 return true;
		  }
	  }
	  return false;
  }

  private void testAdditionalProperties(final JSONObject subject) 
  {
	List<String> addProperties = getAdditionalProperties(subject);
	  
    if (!additionalProperties) 
    {
      Iterator<String> iterator = addProperties.iterator();
      while(iterator.hasNext())
      {
    	failure("extraneous key [%s] is not permitted", iterator.next());
      }      
          
    } else if (schemaOfAdditionalProperties != null)
    {
        Iterator<String> iterator = addProperties.iterator();
        while(iterator.hasNext())
        {
        	schemaOfAdditionalProperties.validate(subject.get(
        			iterator.next()));
        }
    }
  }

  private List<String> getAdditionalProperties(final JSONObject subject) 
  {
	  List<String> addProperties = new ArrayList<String>();
	  String[] names = JSONObject.getNames(subject);
	  int index = 0; 
	  int length = names==null?0:names.length;
	  for(;index < length;index++)
	  {
		  if(!propertySchemas.containsKey(names[index]) && 
				  !matchesAnyPattern(names[index]))
		  {
			  addProperties.add(names[index]);
		  }
	  }
    return addProperties;
  }

  private void testProperties(final JSONObject subject) {
    if (propertySchemas != null) {
      for (Entry<String, Schema> entry : propertySchemas.entrySet()) {
        String key = entry.getKey();
        if (subject.has(key)) {
          entry.getValue().validate(subject.get(key));
        }
      }
    }
  }

  private void testPropertyDependencies(final JSONObject subject)
  {
	  Iterator<String> iterator = propertyDependencies.keySet().iterator();
	  while(iterator.hasNext())
	  {
		 String key = iterator.next();		  
		 if(subject.has(key))
		 {
			 Iterator<String> pIterator = propertyDependencies.get(key).iterator();
			 while(pIterator.hasNext())
			 {
				 String mustBePresent = pIterator.next();
				 if(!subject.has(mustBePresent))
				 {
					 failure("property [%s] is required", mustBePresent);
				 }
			 }
		 }
	  }
  }

  private void testRequiredProperties(final JSONObject subject) 
  {
	  Iterator<String> iterator = requiredProperties.iterator();
	  while(iterator.hasNext())
	  {
		 String key = iterator.next();		  
		 if(!subject.has(key))
		 {
			 failure("required key [%s] not found", key);
		 }
	  }
  }

  private void testSchemaDependencies(final JSONObject subject)
  {
	  Iterator<String> iterator = schemaDependencies.keySet().iterator();
	  while(iterator.hasNext())
	  {
		 String key = iterator.next();
		 if(subject.has(key))
		 {
			 schemaDependencies.get(key).validate(subject);
		 }
	  }
  }

  private void testSize(final JSONObject subject) {
    int actualSize = subject.length();
    if (minProperties != null && actualSize < minProperties.intValue()) {
      throw new ValidationException(String.format("minimum size: [%d], found: [%d]", minProperties,
          actualSize));
    }
    if (maxProperties != null && actualSize > maxProperties.intValue()) {
      throw new ValidationException(String.format("maximum size: [%d], found: [%d]", maxProperties,
          actualSize));
    }
  }

  @Override
  public void validate(final Object subject) {
    if (!(subject instanceof JSONObject)) {
      if (requiresObject) {
        throw new ValidationException(JSONObject.class, subject);
      }
    } else {
      JSONObject objSubject = (JSONObject) subject;
      testProperties(objSubject);
      testRequiredProperties(objSubject);
      testAdditionalProperties(objSubject);
      testSize(objSubject);
      testPropertyDependencies(objSubject);
      testSchemaDependencies(objSubject);
      testPatternProperties(objSubject);
    }
  }

  private void testPatternProperties(final JSONObject subject) {
    String[] propNames = JSONObject.getNames(subject);
    if (propNames == null || propNames.length == 0) {
      return;
    }
    for (Entry<Pattern, Schema> entry : patternProperties.entrySet()) {
      for (String propName : propNames) {
        if (entry.getKey().matcher(propName).find()) {
          entry.getValue().validate(subject.get(propName));
        }
      }
    }
  }

  public boolean requiresObject() {
    return requiresObject;
  }

  public Map<Pattern, Schema> getPatternProperties() {
    return patternProperties;
  }

}
