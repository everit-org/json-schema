package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Arrays.asList;
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

        private final Map<String, Schema> propertySchemas = new HashMap<>();

        private boolean additionalProperties = true;

        private Schema schemaOfAdditionalProperties;

        private final List<String> requiredProperties = new ArrayList<String>(0);

        private Integer minProperties;

        private Integer maxProperties;

        private final Map<String, Set<String>> propertyDependencies = new HashMap<>();

        private final Map<String, Schema> schemaDependencies = new HashMap<>();

        private Schema propertyNameSchema;

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
                dependencies = new HashSet<String>(1);
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

        public Builder propertyNameSchema(Schema propertyNameSchema) {
            this.propertyNameSchema = propertyNameSchema;
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

    private final Schema propertyNameSchema;

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
        this.propertyNameSchema = builder.propertyNameSchema;
    }

    private List<String> getAdditionalProperties(final JSONObject subject) {
        String[] names = JSONObject.getNames(subject);
        if (names == null) {
            return new ArrayList<>();
        } else {
            List<String> namesList = new ArrayList<>();
            for (String name:names) {
                if (!propertySchemas.containsKey(name) && !matchesAnyPattern(name)) {
                    namesList.add(name);
                }
            }
            return namesList;
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

    public Schema getPropertyNameSchema() {
        return propertyNameSchema;
    }

    private Optional<ValidationException> ifFails(final Schema schema, final Object input) {
        try {
            schema.validate(input);
            return Optional.empty();
        } catch (ValidationException e) {
            return Optional.of(e);
        }
    }

    private boolean matchesAnyPattern(final String key) {
        for (Pattern pattern: patternProperties.keySet()) {
            if (pattern.matcher(key).find()) {
                return true;
            }
        }
        return false;
    }

    public boolean permitsAdditionalProperties() {
        return additionalProperties;
    }

    public boolean requiresObject() {
        return requiresObject;
    }

    private void testAdditionalProperties(final JSONObject subject, List<ValidationException> validationExceptions) {
        if (!additionalProperties) {
            List<String> additionalProperties = getAdditionalProperties(subject);
            if (null == additionalProperties || additionalProperties.isEmpty()) {
                return;
            }
            for (String additionalProperty: additionalProperties) {
                validationExceptions.add(new ValidationException(this,
                        format("extraneous key [%s] is not permitted", additionalProperty), "additionalProperties"));
            }
            return;
        } else if (schemaOfAdditionalProperties != null) {
            List<String> additionalPropNames = getAdditionalProperties(subject);
            for (String propName : additionalPropNames) {
                Object propVal = subject.get(propName);
                Optional<ValidationException> exception = ifFails(schemaOfAdditionalProperties, propVal);
                if (exception.isPresent()) {
                    validationExceptions.add(exception.get().prepend(propName, this));
                }
            }
        }
    }

    private void testPatternProperties(final JSONObject subject, List<ValidationException> validationExceptions) {
        String[] propNames = JSONObject.getNames(subject);
        if (propNames == null || propNames.length == 0) {
            return;
        }
        for (Entry<Pattern, Schema> entry : patternProperties.entrySet()) {
            for (String propName : propNames) {
                if (entry.getKey().matcher(propName).find()) {
                    Optional<ValidationException> exception = ifFails(entry.getValue(), subject.get(propName));
                    if (exception.isPresent()) {
                        validationExceptions.add(exception.get().prepend(propName));
                    }
                }
            }
        }
    }

    private void testProperties(final JSONObject subject, List<ValidationException> validationExceptions) {
        if (propertySchemas != null) {
            for (Entry<String, Schema> entry : propertySchemas.entrySet()) {
                String key = entry.getKey();
                if (subject.has(key)) {
                    Optional<ValidationException> exception = ifFails(entry.getValue(), subject.get(key));
                    if (exception.isPresent()) {
                        validationExceptions.add(exception.get().prepend(key));
                    }
                }
            }
        }
    }

    private void testPropertyDependencies(final JSONObject subject, List<ValidationException> validationExceptions) {
        for (String property: propertyDependencies.keySet()) {
            if (subject.has(property)) {
                for (String mustBePresent : propertyDependencies.get(property)) {
                    if (!subject.has(mustBePresent)) {
                        validationExceptions.add(
                                failure(format("property [%s] is required", mustBePresent), "dependencies"));
                    }
                }
            }
        }
    }

    private void testRequiredProperties(final JSONObject subject, List<ValidationException> validationExceptions) {
        for (String required:requiredProperties) {
            if (!subject.has(required)) {
                validationExceptions.add(
                        failure(format("required key [%s] not found", required), "required"));
            }
        }
    }

    private void testSchemaDependencies(final JSONObject subject, List<ValidationException> validationExceptions) {
        for (Map.Entry<String, Schema> schemaDep : schemaDependencies.entrySet()) {
            String propName = schemaDep.getKey();
            if (subject.has(propName)) {
                ifFails(schemaDep.getValue(), subject).ifPresent(validationExceptions::add);
            }
        }
    }

    private void testSize(final JSONObject subject, List<ValidationException> validationExceptions) {
        int actualSize = subject.length();
        if (minProperties != null && actualSize < minProperties.intValue()) {
            validationExceptions.addAll(
                    asList(failure(format("minimum size: [%d], found: [%d]", minProperties, actualSize),
                    "minProperties")));
            return;
        }
        if (maxProperties != null && actualSize > maxProperties.intValue()) {
            validationExceptions.addAll(
                    asList(failure(format("maximum size: [%d], found: [%d]", maxProperties, actualSize),
                    "maxProperties")));
        }
    }

    @Override
    public void validate(final Object subject) {
        if (!(subject instanceof JSONObject)) {
            if (requiresObject) {
                throw failure(JSONObject.class, subject);
            }
        } else {
            List<ValidationException> validationExceptions = new ArrayList<>();
            JSONObject objSubject = (JSONObject) subject;
            testProperties(objSubject, validationExceptions);
            testRequiredProperties(objSubject, validationExceptions);
            testAdditionalProperties(objSubject, validationExceptions);
            testSize(objSubject, validationExceptions);
            testPropertyDependencies(objSubject, validationExceptions);
            testSchemaDependencies(objSubject, validationExceptions);
            testPatternProperties(objSubject, validationExceptions);
            testPropertyNames(objSubject, validationExceptions);
            if (null != validationExceptions) {
                ValidationException.throwFor(this, validationExceptions);
            }
        }
    }

    private void testPropertyNames(JSONObject subject, List<ValidationException> validationExceptions) {
        if (propertyNameSchema != null) {
            String[] names = JSONObject.getNames(subject);
            if (names == null || names.length == 0) {
                return;
            }
            for (String name: names) {
                try {
                    propertyNameSchema.validate(name);
                } catch (ValidationException e) {
                    validationExceptions.add(e.prepend(name));
                }
            }
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
        for (Pattern pattern: patternProperties.keySet()) {
            if (pattern.matcher(current).matches()) {
                if (remaining == null || patternProperties.get(pattern).definesProperty(remaining)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean definesSchemaDependencyProperty(final String field) {
        if (schemaDependencies.containsKey(field)) {
            return true;
        }
        for (Schema schema: schemaDependencies.values()) {
            if (schema.definesProperty(field)) {
                return true;
            }
        }
        return false;
    }

    private String unescape(final String value) {
        return value.replace("~1", "/").replace("~0", "~");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
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
                    Objects.equals(propertyNameSchema, that.propertyNameSchema) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertySchemas, propertyNameSchema, additionalProperties, schemaOfAdditionalProperties, requiredProperties,
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
        if (propertyNameSchema != null) {
            writer.key("propertyNames");
            propertyNameSchema.describeTo(writer);
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
        propertyDependencies.entrySet().forEach(entry -> {
            writer.key(entry.getKey());
            writer.array();
            entry.getValue().forEach(writer::value);
            writer.endArray();
        });
        writer.endObject();
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ObjectSchema;
    }
}
