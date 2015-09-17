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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;

/**
 * Javadoc.
 */
public class ObjectSchema implements Schema {

  public static class Builder {

    private final Map<String, Schema> propertySchemas = new HashMap<>();;

    private boolean additionalProperties = true;

    private Schema schemaOfAdditionalProperties;

    private final List<String> requiredProperties = new ArrayList<String>(0);

    private Integer minProperties;

    private Integer maxProperties;

    public Builder minProperties(final Integer minProperties) {
      this.minProperties = minProperties;
      return this;
    }

    public Builder maxProperties(final Integer maxProperties) {
      this.maxProperties = maxProperties;
      return this;
    }

    public Builder addRequiredProperty(final String propertyName) {
      requiredProperties.add(propertyName);
      return this;
    }

    public Builder addPropertySchema(final String propName, final Schema schema) {
      Objects.requireNonNull(propName, "propName cannot be null");
      Objects.requireNonNull(schema, "schema cannot be null");
      propertySchemas.put(propName, schema);
      return this;
    }

    public Builder schemaOfAdditionalProperties(final Schema schemaOfAdditionalProperties) {
      this.schemaOfAdditionalProperties = schemaOfAdditionalProperties;
      return this;
    }

    public Builder additionalProperties(final boolean additionalProperties) {
      this.additionalProperties = additionalProperties;
      return this;
    }

    public ObjectSchema build() {
      return new ObjectSchema(this);
    }

  }

  public ObjectSchema(final Builder builder) {
    this.propertySchemas = builder.propertySchemas == null ? null :
        Collections.unmodifiableMap(builder.propertySchemas);
    this.additionalProperties = builder.additionalProperties;
    this.schemaOfAdditionalProperties = builder.schemaOfAdditionalProperties;
    if (!additionalProperties && schemaOfAdditionalProperties != null) {
      throw new SchemaException(
          "additionalProperties cannot be false if schemaOfAdditionalProperties is present");
    }
    this.requiredProperties = Collections.unmodifiableList(new ArrayList<>(
        builder.requiredProperties));
    this.minProperties = builder.minProperties;
    this.maxProperties = builder.maxProperties;
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

  @Override
  public void validate(final Object subject) {
    if (!(subject instanceof JSONObject)) {
      throw new ValidationException(JSONObject.class, subject);
    }
    JSONObject objSubject = (JSONObject) subject;
    testProperties(objSubject);
    testRequiredProperties(objSubject);
    testAdditionalProperties(objSubject);
    testSize(objSubject);
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

  private void failure(final String exceptionMessage, final Object... params) {
    throw new ValidationException(String.format(exceptionMessage, params));
  }

  private void testAdditionalProperties(final JSONObject subject) {
    if (!additionalProperties) {
      Arrays
          .stream(JSONObject.getNames(subject))
          .filter(key -> !propertySchemas.containsKey(key))
          .findFirst()
          .ifPresent(unneeded -> failure("extraneous key [%s] is not permitted", unneeded));
    }
  }

  private void testRequiredProperties(final JSONObject subject) {
    requiredProperties.stream()
        .filter(key -> !subject.has(key))
        .findFirst()
        .ifPresent(missing -> failure("required key [%s] not found", missing));
  }

  private void testProperties(final JSONObject subject) {
    if (propertySchemas != null) {
      for (String schemaPropKey : propertySchemas.keySet()) {
        if (subject.has(schemaPropKey)) {
          propertySchemas.get(schemaPropKey).validate(subject.get(schemaPropKey));
        }
      }
    }
  }

  public Map<String, Schema> getPropertySchemas() {
    return propertySchemas;
  }

  public boolean permitsAdditionalProperties() {
    return additionalProperties;
  }

  public Schema getSchemaOfAdditionalProperties() {
    return schemaOfAdditionalProperties;
  }

  public List<String> getRequiredProperties() {
    return requiredProperties;
  }

  public Integer getMinProperties() {
    return minProperties;
  }

  public Integer getMaxProperties() {
    return maxProperties;
  }

}
