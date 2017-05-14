package org.everit.json.schema;

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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

    private Stream<String> getAdditionalProperties(final JSONObject subject) {
        String[] names = JSONObject.getNames(subject);
        if (names == null) {
            return Stream.empty();
        } else {
            return Arrays
                    .stream(names)
                    .filter(key -> !propertySchemas.containsKey(key))
                    .filter(key -> !matchesAnyPattern(key));
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
            return Optional.empty();
        } catch (ValidationException e) {
            return Optional.of(e);
        }
    }

    private boolean matchesAnyPattern(final String key) {
        return patternProperties.keySet().stream()
                .filter(pattern -> pattern.matcher(key).find())
                .findAny()
                .isPresent();
    }

    public boolean permitsAdditionalProperties() {
        return additionalProperties;
    }

    public boolean requiresObject() {
        return requiresObject;
    }

    private List<ValidationException> testAdditionalProperties(final JSONObject subject) {
        if (!additionalProperties) {
            return getAdditionalProperties(subject)
                    .map(unneeded -> format("extraneous key [%s] is not permitted", unneeded))
                    .map(msg -> new ValidationException(this, msg, "additionalProperties"))
                    .collect(Collectors.toList());
        } else if (schemaOfAdditionalProperties != null) {
            List<String> additionalPropNames = getAdditionalProperties(subject)
                    .collect(Collectors.toList());
            List<ValidationException> rval = new ArrayList<ValidationException>();
            for (String propName : additionalPropNames) {
                Object propVal = subject.get(propName);
                ifFails(schemaOfAdditionalProperties, propVal)
                        .map(failure -> failure.prepend(propName, this))
                        .ifPresent(rval::add);
            }
            return rval;
        }
        return emptyList();
    }

    private List<ValidationException> testPatternProperties(final JSONObject subject) {
        String[] propNames = JSONObject.getNames(subject);
        if (propNames == null || propNames.length == 0) {
            return emptyList();
        }
        List<ValidationException> rval = new ArrayList<>();
        for (Entry<Pattern, Schema> entry : patternProperties.entrySet()) {
            for (String propName : propNames) {
                if (entry.getKey().matcher(propName).find()) {
                    ifFails(entry.getValue(), subject.get(propName))
                            .map(exc -> exc.prepend(propName))
                            .ifPresent(rval::add);
                }
            }
        }
        return rval;
    }

    private List<ValidationException> testProperties(final JSONObject subject) {
        if (propertySchemas != null) {
            List<ValidationException> rval = new ArrayList<>();
            for (Entry<String, Schema> entry : propertySchemas.entrySet()) {
                String key = entry.getKey();
                if (subject.has(key)) {
                    ifFails(entry.getValue(), subject.get(key))
                            .map(exc -> exc.prepend(key))
                            .ifPresent(rval::add);
                }
            }
            return rval;
        }
        return emptyList();
    }

    private List<ValidationException> testPropertyDependencies(final JSONObject subject) {
        return propertyDependencies.keySet().stream()
                .filter(subject::has)
                .flatMap(ifPresent -> propertyDependencies.get(ifPresent).stream())
                .filter(mustBePresent -> !subject.has(mustBePresent))
                .map(missingKey -> format("property [%s] is required", missingKey))
                .map(excMessage -> failure(excMessage, "dependencies"))
                .collect(Collectors.toList());
    }

    private List<ValidationException> testRequiredProperties(final JSONObject subject) {
        return requiredProperties.stream()
                .filter(key -> !subject.has(key))
                .map(missingKey -> format("required key [%s] not found", missingKey))
                .map(excMessage -> failure(excMessage, "required"))
                .collect(Collectors.toList());
    }

    private List<ValidationException> testSchemaDependencies(final JSONObject subject) {
        List<ValidationException> rval = new ArrayList<>();
        for (Map.Entry<String, Schema> schemaDep : schemaDependencies.entrySet()) {
            String propName = schemaDep.getKey();
            if (subject.has(propName)) {
                ifFails(schemaDep.getValue(), subject).ifPresent(rval::add);
            }
        }
        return rval;
    }

    private List<ValidationException> testSize(final JSONObject subject) {
        int actualSize = subject.length();
        if (minProperties != null && actualSize < minProperties.intValue()) {
            return asList(failure(format("minimum size: [%d], found: [%d]", minProperties, actualSize),
                    "minProperties"));
        }
        if (maxProperties != null && actualSize > maxProperties.intValue()) {
            return asList(failure(format("maximum size: [%d], found: [%d]", maxProperties, actualSize),
                    "maxProperties"));
        }
        return emptyList();
    }

    @Override
    public void validate(final Object subject) {
        if (!(subject instanceof JSONObject)) {
            if (requiresObject) {
                throw failure(JSONObject.class, subject);
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
        return patternProperties.keySet()
                .stream()
                .filter(pattern -> pattern.matcher(current).matches())
                .map(pattern -> patternProperties.get(pattern))
                .filter(schema -> remaining == null || schema.definesProperty(remaining))
                .findAny()
                .isPresent();
    }

    private boolean definesSchemaDependencyProperty(final String field) {
        return schemaDependencies.containsKey(field)
                || schemaDependencies.values().stream()
                .filter(schema -> schema.definesProperty(field))
                .findAny()
                .isPresent();
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
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertySchemas, additionalProperties, schemaOfAdditionalProperties, requiredProperties,
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
