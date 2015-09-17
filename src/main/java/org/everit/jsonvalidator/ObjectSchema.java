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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Javadoc.
 */
public class ObjectSchema implements Schema {

  public static class Builder {

    private Map<String, Schema> propertySchemas;

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
      if (propertySchemas == null) {
        propertySchemas = new HashMap<>();
      }
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
