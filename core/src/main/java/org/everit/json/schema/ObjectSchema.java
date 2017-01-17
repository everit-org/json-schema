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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Object schema validator.
 */
public class ObjectSchema extends Schema {

    /**
     * Builder class for {@link ObjectSchema}.
     */
    public static class Builder extends Schema.Builder<ObjectSchema> {

        private final Map<Pattern, Schema> patternProperties = new HashMap<>();

        private boolean requiresObject = true;

        private final Map<String, Schema> propertySchemas = new LinkedHashMap<>();

        private boolean additionalProperties = true;

        private Schema schemaOfAdditionalProperties;

        private final List<String> requiredProperties = new ArrayList<String>(0);

        private Integer minProperties;

        private Integer maxProperties;

        private final Map<String, Set<String>> propertyDependencies = new HashMap<>();

        private final Map<String, Schema> schemaDependencies = new HashMap<>();

        public Builder additionalProperties(final boolean additionalProperties) {
            this.additionalProperties = additionalProperties;
            return this;
        }

        /**
         * Adds a property schema.
         *
         * @param propName the name of the property which' expected schema must be {@code schema}
         * @param schema   if the subject under validation has a property named {@code propertyName} then its
         *                 value will be validated using this {@code schema}
         * @return {@code this}
         */
        public Builder addPropertySchema(final String propName, final Schema schema) {
            requireNonNull(propName, "propName cannot be null");
            requireNonNull(schema, "schema cannot be null");
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

        public Builder patternProperty(final Pattern pattern, final Schema schema) {
            this.patternProperties.put(pattern, schema);
            return this;
        }

        public Builder patternProperty(final String pattern, final Schema schema) {
            return patternProperty(Pattern.compile(pattern), schema);
        }

        /**
         * Adds a property dependency.
         *
         * @param ifPresent     the name of the property which if is present then a property with name
         *                      {@code mustBePresent} is mandatory
         * @param mustBePresent a property with this name must exist in the subject under validation if a property
         *                      named {@code ifPresent} exists
         * @return {@code this}
         */
        public Builder propertyDependency(final String ifPresent, final String mustBePresent) {
            Set<String> dependencies = propertyDependencies.get(ifPresent);
            if (dependencies == null) {
                dependencies = new LinkedHashSet<>(1);
                propertyDependencies.put(ifPresent, dependencies);
            }
            dependencies.add(mustBePresent);
            return this;
        }

        public Builder requiresObject(final boolean requiresObject) {
            this.requiresObject = requiresObject;
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

    private static <K, V> Map<K, V> copyMap(final Map<K, V> original) {
        return Collections.unmodifiableMap(new HashMap<>(original));
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
     * @param builder the builder object containing validation criteria
     */
    public ObjectSchema(final Builder builder) {
        super(builder);
        this.propertySchemas = builder.propertySchemas == null ? null
                : Collections.unmodifiableMap(builder.propertySchemas);
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
        this.propertyDependencies = copyMap(builder.propertyDependencies);
        this.schemaDependencies = copyMap(builder.schemaDependencies);
        this.requiresObject = builder.requiresObject;
        this.patternProperties = copyMap(builder.patternProperties);
    }

    private FluentIterable<String> getAdditionalProperties(final JSONObject subject) {
        String[] names = JSONObject.getNames(subject);
        if (names == null) {
            return FluentIterable.from(Lists.<String>newArrayList());
        } else {
            return FluentIterable.of(names)
                    .filter(new Predicate<String>() {
                        @Override
                        public boolean apply(String key) {
                            return !propertySchemas.containsKey(key) && !matchesAnyPattern(key);
                        }
                    });
        }
    }

    public Integer getMaxProperties() {
        return maxProperties;
    }

    public Integer getMinProperties() {
        return minProperties;
    }

    public Map<Pattern, Schema> getPatternProperties() {
        return patternProperties;
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

    private Optional<ValidationException> ifFails(final Schema schema, final Object input) {
        try {
            schema.validate(input);
            return Optional.absent();
        } catch (ValidationException e) {
            return Optional.of(e);
        }
    }

    private boolean matchesAnyPattern(final String key) {
        return FluentIterable.from(patternProperties.keySet())
                .firstMatch(new Predicate<Pattern>() {
                    @Override
                    public boolean apply(Pattern pattern) {
                        return pattern.matcher(key).find();
                    }
                })
                .isPresent();
    }

    public boolean permitsAdditionalProperties() {
        return additionalProperties;
    }

    public boolean requiresObject() {
        return requiresObject;
    }

    private ImmutableList<ValidationException> testAdditionalProperties(final JSONObject subject) {
        if (!additionalProperties) {
            return getAdditionalProperties(subject)
                    .transform(new Function<String, ValidationException>() {
                        @Override
                        public ValidationException apply(String unneeded) {
                            return new ValidationException(ObjectSchema.this,
                                    String.format("extraneous key [%s] is not permitted", unneeded), "additionalProperties");
                        }
                    })
                    .toList();
        } else if (schemaOfAdditionalProperties != null) {
            List<String> additionalPropNames = getAdditionalProperties(subject).toList();
            List<ValidationException> rval = new ArrayList<ValidationException>();
            for (final String propName : additionalPropNames) {
                Object propVal = subject.get(propName);
                rval.addAll(ifFails(schemaOfAdditionalProperties, propVal)
                        .transform(new Function<ValidationException, ValidationException>() {
                            @Override
                            public ValidationException apply(ValidationException failure) {
                                return failure.prepend(propName, ObjectSchema.this);
                            }
                        }).asSet());
            }
            return ImmutableList.copyOf(rval);
        }
        return ImmutableList.of();
    }

    private List<ValidationException> testPatternProperties(final JSONObject subject) {
        String[] propNames = JSONObject.getNames(subject);
        if (propNames == null || propNames.length == 0) {
            return Collections.emptyList();
        }
        List<ValidationException> rval = new ArrayList<>();
        for (Entry<Pattern, Schema> entry : patternProperties.entrySet()) {
            for (final String propName : propNames) {
                if (entry.getKey().matcher(propName).find()) {
                    rval.addAll(ifFails(entry.getValue(), subject.get(propName))
                            .transform(new Function<ValidationException, ValidationException>() {
                                @Override
                                public ValidationException apply(ValidationException exc) {
                                    return exc.prepend(propName);
                                }
                            })
                            .asSet());
                }
            }
        }
        return rval;
    }

    private List<ValidationException> testProperties(final JSONObject subject) {
        if (propertySchemas != null) {
            List<ValidationException> rval = new ArrayList<>();
            for (Entry<String, Schema> entry : propertySchemas.entrySet()) {
                final String key = entry.getKey();
                if (subject.has(key)) {
                    rval.addAll(ifFails(entry.getValue(), subject.get(key))
                            .transform(new Function<ValidationException, ValidationException>() {
                                @Override
                                public ValidationException apply(ValidationException exc) {
                                    return exc.prepend(key);
                                }
                            })
                            .asSet());
                }
            }
            return rval;
        }
        return Collections.emptyList();
    }

    private ImmutableList<ValidationException> testPropertyDependencies(final JSONObject subject) {
        return FluentIterable.from(propertyDependencies.keySet())
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return subject.has(input);
                    }
                })
                .transformAndConcat(new Function<String, Set<String>>() {
                    @Override
                    public Set<String> apply(String input) {
                        return propertyDependencies.get(input);
                    }
                })
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String mustBePresent) {
                        return !subject.has(mustBePresent);
                    }
                })
                .transform(new Function<String, ValidationException>() {
                    @Override
                    public ValidationException apply(String missingKey) {
                        return new ValidationException(ObjectSchema.this,
                                String.format("property [%s] is required", missingKey), "dependencies");
                    }
                })
                .toList();
    }

    private List<ValidationException> testRequiredProperties(final JSONObject subject) {
        return FluentIterable.from(requiredProperties)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String key) {
                        return !subject.has(key);
                    }
                })
                .transform(new Function<String, ValidationException>() {
                    @Override
                    public ValidationException apply(String missingKey) {
                        return new ValidationException(ObjectSchema.this,
                                String.format("required key [%s] not found", missingKey), "required");
                    }
                })
                .toList();
    }

    private List<ValidationException> testSchemaDependencies(final JSONObject subject) {
        List<ValidationException> rval = new ArrayList<>();
        for (Map.Entry<String, Schema> schemaDep : schemaDependencies.entrySet()) {
            String propName = schemaDep.getKey();
            if (subject.has(propName)) {
                rval.addAll(ifFails(schemaDep.getValue(), subject).asSet());
            }
        }
        return rval;
    }

    private List<ValidationException> testSize(final JSONObject subject) {
        int actualSize = subject.length();
        if (minProperties != null && actualSize < minProperties.intValue()) {
            return Arrays
                    .asList(new ValidationException(this, String.format("minimum size: [%d], found: [%d]",
                            minProperties, actualSize), "minProperties"));
        }
        if (maxProperties != null && actualSize > maxProperties.intValue()) {
            return Arrays
                    .asList(new ValidationException(this, String.format("maximum size: [%d], found: [%d]",
                            maxProperties, actualSize), "maxProperties"));
        }
        return Collections.emptyList();
    }

    @Override
    public void validate(final Object subject) {
        if (!(subject instanceof JSONObject)) {
            if (requiresObject) {
                throw new ValidationException(this, JSONObject.class, subject);
            }
        } else {
            List<ValidationException> failures = new ArrayList<>();
            JSONObject objSubject = (JSONObject) subject;
            failures.addAll(testProperties(objSubject));
            failures.addAll(testRequiredProperties(objSubject));
            failures.addAll(testAdditionalProperties(objSubject));
            failures.addAll(testSize(objSubject));
            failures.addAll(testPropertyDependencies(objSubject));
            failures.addAll(testSchemaDependencies(objSubject));
            failures.addAll(testPatternProperties(objSubject));
            ValidationException.throwFor(this, failures);
        }
    }

    @Override
    public boolean definesProperty(String field) {
        field = field.replaceFirst("^#", "").replaceFirst("^/", "");
        int firstSlashIdx = field.indexOf('/');
        String nextToken, remaining;
        if (firstSlashIdx == -1) {
            nextToken = field;
            remaining = null;
        } else {
            nextToken = field.substring(0, firstSlashIdx);
            remaining = field.substring(firstSlashIdx + 1);
        }
        return !field.isEmpty() && (definesSchemaProperty(nextToken, remaining)
                || definesPatternProperty(nextToken, remaining)
                || definesSchemaDependencyProperty(field));
    }

    private boolean definesSchemaProperty(String current, final String remaining) {
        current = unescape(current);
        boolean hasSuffix = !(remaining == null);
        if (propertySchemas.containsKey(current)) {
            if (hasSuffix) {
                return propertySchemas.get(current).definesProperty(remaining);
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean definesPatternProperty(final String current, final String remaining) {
        return FluentIterable.from(patternProperties.keySet())
                .filter(new Predicate<Pattern>() {
                    @Override
                    public boolean apply(Pattern pattern) {
                        return pattern.matcher(current).matches();
                    }
                })
                .transform(new Function<Pattern, Schema>() {
                    @Override
                    public Schema apply(Pattern pattern) {
                        return patternProperties.get(pattern);
                    }
                })
                .firstMatch(new Predicate<Schema>() {
                    @Override
                    public boolean apply(Schema schema) {
                        return remaining == null || schema.definesProperty(remaining);
                    }
                })
                .isPresent();
    }

    private boolean definesSchemaDependencyProperty(final String field) {
        return schemaDependencies.containsKey(field)
                || FluentIterable.from(schemaDependencies.values())
                .firstMatch(new Predicate<Schema>() {
                    @Override
                    public boolean apply(Schema schema) {
                        return schema.definesProperty(field);
                    }
                })
                .isPresent();
    }

    private String unescape(final String value) {
        return value.replace("~1", "/").replace("~0", "~");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ObjectSchema) {
            ObjectSchema that = (ObjectSchema) o;
            return that.canEqual(this) &&
                    additionalProperties == that.additionalProperties &&
                    requiresObject == that.requiresObject &&
                    Objects.equals(propertySchemas, that.propertySchemas) &&
                    Objects.equals(schemaOfAdditionalProperties, that.schemaOfAdditionalProperties) &&
                    Objects.equals(requiredProperties, that.requiredProperties) &&
                    Objects.equals(minProperties, that.minProperties) &&
                    Objects.equals(maxProperties, that.maxProperties) &&
                    Objects.equals(propertyDependencies, that.propertyDependencies) &&
                    Objects.equals(schemaDependencies, that.schemaDependencies) &&
                    Objects.equals(patternProperties, that.patternProperties) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertySchemas, additionalProperties, schemaOfAdditionalProperties,
                requiredProperties,
                minProperties, maxProperties, propertyDependencies, schemaDependencies, requiresObject, patternProperties);
    }

    @Override
    void describePropertiesTo(JSONPrinter writer) {
        if (requiresObject) {
            writer.key("type").value("object");
        }
        if (!propertySchemas.isEmpty()) {
            writer.key("properties");
            writer.printSchemaMap(propertySchemas);
        }
        writer.ifPresent("minProperties", minProperties);
        writer.ifPresent("maxProperties", maxProperties);
        if (!requiredProperties.isEmpty()) {
            writer.key("required").value(requiredProperties);
        }
        if (schemaOfAdditionalProperties != null) {
            writer.key("additionalProperties");
            schemaOfAdditionalProperties.describeTo(writer);
        }
        if (!propertyDependencies.isEmpty()) {
            describePropertyDependenciesTo(writer);
        }
        if (!schemaDependencies.isEmpty()) {
            writer.key("dependencies");
            writer.printSchemaMap(schemaDependencies);
        }
        if (!patternProperties.isEmpty()) {
            writer.key("patternProperties");
            writer.printSchemaMap(patternProperties);
        }
        writer.ifFalse("additionalProperties", additionalProperties);
    }

    private void describePropertyDependenciesTo(JSONPrinter writer) {
        writer.key("dependencies");
        writer.object();
        for (Entry<String, Set<String>> entry : propertyDependencies.entrySet()) {
            writer.key(entry.getKey());
            writer.array();
            for (String value : entry.getValue()) {
                writer.value(value);
            }
            writer.endArray();
        }
        writer.endObject();
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ObjectSchema;
    }
}
