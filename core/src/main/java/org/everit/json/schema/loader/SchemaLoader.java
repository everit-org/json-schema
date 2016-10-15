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

import org.everit.json.schema.*;
import org.everit.json.schema.internal.*;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.everit.json.schema.loader.internal.JSONPointer;
import org.everit.json.schema.loader.internal.JSONPointer.QueryResult;
import org.everit.json.schema.loader.internal.ReferenceResolver;
import org.everit.json.schema.loader.internal.WrappingFormatValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

/**
 * Loads a JSON schema's JSON representation into schema validator instances.
 */
public class SchemaLoader {

    /**
     * Alias for {@code Function<Collection<Schema>, CombinedSchema.Builder>}.
     */
    @FunctionalInterface
    private interface CombinedSchemaProvider
            extends Function<Collection<Schema>, CombinedSchema.Builder> {

    }

    /**
     * Builder class for {@link SchemaLoader}.
     */
    public static class SchemaLoaderBuilder {

        SchemaClient httpClient = new DefaultSchemaClient();

        JSONObject schemaJson;

        JSONObject rootSchemaJson;

        Map<String, ReferenceSchema.Builder> pointerSchemas = new HashMap<>();

        URI id;

        Map<String, FormatValidator> formatValidators = new HashMap<>();

        {
            formatValidators.put("date-time", new DateTimeFormatValidator());
            formatValidators.put("uri", new URIFormatValidator());
            formatValidators.put("email", new EmailFormatValidator());
            formatValidators.put("ipv4", new IPV4Validator());
            formatValidators.put("ipv6", new IPV6Validator());
            formatValidators.put("hostname", new HostnameFormatValidator());
        }

        /**
         * Registers a format validator with the name returned by {@link FormatValidator#formatName()}.
         *
         * @param formatValidator
         * @return {@code this}
         */
        public SchemaLoaderBuilder addFormatValidator(FormatValidator formatValidator) {
            formatValidators.put(formatValidator.formatName(), formatValidator);
            return this;
        }

        /**
         * @param formatName      the name which will be used in the schema JSON files to refer to this {@code formatValidator}
         * @param formatValidator the object performing the validation for schemas which use the {@code formatName} format
         * @return {@code this}
         * @deprecated instead it is better to override {@link FormatValidator#formatName()}
         * and use {@link #addFormatValidator(FormatValidator)}
         */
        @Deprecated
        public SchemaLoaderBuilder addFormatValidator(final String formatName,
                final FormatValidator formatValidator) {
            if (!Objects.equals(formatName, formatValidator.formatName())) {
                formatValidators.put(formatName, new WrappingFormatValidator(formatName, formatValidator));
            } else {
                formatValidators.put(formatName, formatValidator);
            }
            return this;
        }

        public SchemaLoader build() {
            return new SchemaLoader(this);
        }

        public JSONObject getRootSchemaJson() {
            return rootSchemaJson == null ? schemaJson : rootSchemaJson;
        }

        public SchemaLoaderBuilder httpClient(final SchemaClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Sets the initial resolution scope of the schema. {@code id} and {@code $ref} attributes
         * accuring in the schema will be resolved against this value.
         *
         * @param id the initial (absolute) URI, used as the resolution scope.
         * @return {@code this}
         */
        public SchemaLoaderBuilder resolutionScope(final String id) {
            try {
                return resolutionScope(new URI(id));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        public SchemaLoaderBuilder resolutionScope(final URI id) {
            this.id = id;
            return this;
        }

        SchemaLoaderBuilder pointerSchemas(final Map<String, ReferenceSchema.Builder> pointerSchemas) {
            this.pointerSchemas = pointerSchemas;
            return this;
        }

        SchemaLoaderBuilder rootSchemaJson(final JSONObject rootSchemaJson) {
            this.rootSchemaJson = rootSchemaJson;
            return this;
        }

        public SchemaLoaderBuilder schemaJson(final JSONObject schemaJson) {
            this.schemaJson = schemaJson;
            return this;
        }

        SchemaLoaderBuilder formatValidators(final Map<String, FormatValidator> formatValidators) {
            this.formatValidators = formatValidators;
            return this;
        }

    }

    private static final List<String> ARRAY_SCHEMA_PROPS = asList("items", "additionalItems",
            "minItems",
            "maxItems",
            "uniqueItems");

    private static final Map<String, CombinedSchemaProvider> COMB_SCHEMA_PROVIDERS = new HashMap<>(3);

    private static final List<String> NUMBER_SCHEMA_PROPS = asList("minimum", "maximum",
            "minimumExclusive", "maximumExclusive", "multipleOf");

    private static final List<String> OBJECT_SCHEMA_PROPS = asList("properties", "required",
            "minProperties",
            "maxProperties",
            "dependencies",
            "patternProperties",
            "additionalProperties");

    private static final List<String> STRING_SCHEMA_PROPS = asList("minLength", "maxLength",
            "pattern", "format");

    static {
        COMB_SCHEMA_PROVIDERS.put("allOf", CombinedSchema::allOf);
        COMB_SCHEMA_PROVIDERS.put("anyOf", CombinedSchema::anyOf);
        COMB_SCHEMA_PROVIDERS.put("oneOf", CombinedSchema::oneOf);
    }

    public static SchemaLoaderBuilder builder() {
        return new SchemaLoaderBuilder();
    }

    /**
     * Loads a JSON schema to a schema validator using a {@link DefaultSchemaClient default HTTP
     * client}.
     *
     * @param schemaJson the JSON representation of the schema.
     * @return the schema validator object
     */
    public static Schema load(final JSONObject schemaJson) {
        return SchemaLoader.load(schemaJson, new DefaultSchemaClient());
    }

    /**
     * Creates Schema instance from its JSON representation.
     *
     * @param schemaJson the JSON representation of the schema.
     * @param httpClient the HTTP client to be used for resolving remote JSON references.
     * @return the created schema
     */
    public static Schema load(final JSONObject schemaJson, final SchemaClient httpClient) {
        SchemaLoader loader = builder()
                .schemaJson(schemaJson)
                .httpClient(httpClient)
                .build();
        return loader.load().build();
    }

    /**
     * Returns the absolute URI without its fragment part.
     *
     * @param fullUri the abslute URI
     * @return the URI without the fragment part
     */
    static URI withoutFragment(final String fullUri) {
        int hashmarkIdx = fullUri.indexOf('#');
        String rval;
        if (hashmarkIdx == -1) {
            rval = fullUri;
        } else {
            rval = fullUri.substring(0, hashmarkIdx);
        }
        try {
            return new URI(rval);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final LoadingState ls;


    /**
     * Constructor.
     *
     * @param builder the builder containing the properties. Only {@link SchemaLoaderBuilder#id} is
     *                nullable.
     * @throws NullPointerException if any of the builder properties except {@link SchemaLoaderBuilder#id id} is
     *                              {@code null}.
     */
    public SchemaLoader(final SchemaLoaderBuilder builder) {
        URI id = builder.id;
        if (id == null && builder.schemaJson.has("id")) {
            try {
                id = new URI(builder.schemaJson.getString("id"));
            } catch (JSONException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        this.ls = new LoadingState(builder.httpClient,
                builder.formatValidators,
                builder.pointerSchemas,
                builder.getRootSchemaJson(),
                builder.schemaJson,
                id);
    }

    /**
     * Constructor.
     *
     * @deprecated use {@link SchemaLoader#SchemaLoader(SchemaLoaderBuilder)} instead.
     */
    @Deprecated SchemaLoader(final String id, final JSONObject schemaJson,
            final JSONObject rootSchemaJson, final Map<String, ReferenceSchema.Builder> pointerSchemas,
            final SchemaClient httpClient) {
        this(builder().schemaJson(schemaJson)
                .rootSchemaJson(rootSchemaJson)
                .resolutionScope(id)
                .httpClient(httpClient)
                .pointerSchemas(pointerSchemas));
    }

    private void addFormatValidator(final StringSchema.Builder builder, final String formatName) {
        getFormatValidator(formatName).ifPresent(builder::formatValidator);
    }

    private CombinedSchema.Builder buildAnyOfSchemaForMultipleTypes() {
        JSONArray subtypeJsons = ls.schemaJson.getJSONArray("type");
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

    private EnumSchema.Builder buildEnumSchema() {
        Set<Object> possibleValues = new HashSet<>();
        JSONArray arr = ls.schemaJson.getJSONArray("enum");
        IntStream.range(0, arr.length())
                .mapToObj(arr::get)
                .forEach(possibleValues::add);
        return EnumSchema.builder().possibleValues(possibleValues);
    }

    private NotSchema.Builder buildNotSchema() {
        Schema mustNotMatch = loadChild(ls.schemaJson.getJSONObject("not")).build();
        return NotSchema.builder().mustNotMatch(mustNotMatch);
    }

    private NumberSchema.Builder buildNumberSchema() {
        NumberSchema.Builder builder = NumberSchema.builder();
        ls.ifPresent("minimum", Number.class, builder::minimum);
        ls.ifPresent("maximum", Number.class, builder::maximum);
        ls.ifPresent("multipleOf", Number.class, builder::multipleOf);
        ls.ifPresent("exclusiveMinimum", Boolean.class, builder::exclusiveMinimum);
        ls.ifPresent("exclusiveMaximum", Boolean.class, builder::exclusiveMaximum);
        return builder;
    }

    private Schema.Builder<?> buildSchemaWithoutExplicitType() {
        if (ls.schemaJson.length() == 0) {
            return EmptySchema.builder();
        }
        if (ls.schemaJson.has("$ref")) {
            return lookupReference(ls.schemaJson.getString("$ref"), ls.schemaJson);
        }
        Schema.Builder<?> rval = sniffSchemaByProps();
        if (rval != null) {
            return rval;
        }
        if (ls.schemaJson.has("not")) {
            return buildNotSchema();
        }
        return EmptySchema.builder();
    }

    private StringSchema.Builder buildStringSchema() {
        StringSchema.Builder builder = StringSchema.builder();
        ls.ifPresent("minLength", Integer.class, builder::minLength);
        ls.ifPresent("maxLength", Integer.class, builder::maxLength);
        ls.ifPresent("pattern", String.class, builder::pattern);
        ls.ifPresent("format", String.class, format -> addFormatValidator(builder, format));
        return builder;
    }

    /**
     * Underscore-like extend function. Merges the properties of {@code additional} and
     * {@code original}. Neither {@code additional} nor {@code original} will be modified, but the
     * returned object may be referentially the same as one of the parameters (in case the other
     * parameter is an empty object).
     */
    JSONObject extend(final JSONObject additional, final JSONObject original) {
        String[] additionalNames = JSONObject.getNames(additional);
        if (additionalNames == null) {
            return original;
        }
        String[] originalNames = JSONObject.getNames(original);
        if (originalNames == null) {
            return additional;
        }
        JSONObject rval = new JSONObject();
        Arrays.stream(originalNames).forEach(name -> rval.put(name, original.get(name)));
        Arrays.stream(additionalNames).forEach(name -> rval.put(name, additional.get(name)));
        return rval;
    }

    Optional<FormatValidator> getFormatValidator(final String format) {
        return Optional.ofNullable(formatValidators.get(format));
    }

    /**
     * Populates a {@code Schema.Builder} instance from the {@code schemaJson} schema definition.
     *
     * @return the builder which already contains the validation criteria of the schema, therefore
     * {@link Schema.Builder#build()} can be immediately used to acquire the {@link Schema}
     * instance to be used for validation
     */
    public Schema.Builder<?> load() {
        Schema.Builder<?> builder;
        if (ls.schemaJson.has("enum")) {
            builder = buildEnumSchema();
        } else {
            builder = tryCombinedSchema();
            if (builder == null) {
                if (!ls.schemaJson.has("type") || ls.schemaJson.has("$ref")) {
                    builder = buildSchemaWithoutExplicitType();
                } else {
                    builder = loadForType(ls.schemaJson.get("type"));
                }
            }
        }
        ls.ifPresent("id", String.class, builder::id);
        ls.ifPresent("title", String.class, builder::title);
        ls.ifPresent("description", String.class, builder::description);
        return builder;
    }

    private Schema.Builder<?> loadChild(final JSONObject childJson) {
        return selfBuilder().schemaJson(childJson).build().load();
    }

    private Schema.Builder<?> loadForExplicitType(final String typeString) {
        switch (typeString) {
        case "string":
            return buildStringSchema();
        case "integer":
            return buildNumberSchema().requiresInteger(true);
        case "number":
            return buildNumberSchema();
        case "boolean":
            return BooleanSchema.builder();
        case "null":
            return NullSchema.builder();
        case "array":
            return buildArraySchema();
        case "object":
            return buildObjectSchema();
        default:
            throw new SchemaException(String.format("unknown type: [%s]", typeString));
        }
    }

    private Schema.Builder<?> loadForType(Object type) {
        if (type instanceof JSONArray) {
            return buildAnyOfSchemaForMultipleTypes();
        } else if (type instanceof String) {
            return loadForExplicitType((String) type);
        } else {
            throw new SchemaException("type", asList(JSONArray.class, String.class), type);
        }
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    private Schema.Builder<?> lookupReference(String relPointerString, JSONObject ctx) {
        String absPointerString = ReferenceResolver.resolve(ls.id, relPointerString).toString();
        if (ls.pointerSchemas.containsKey(absPointerString)) {
            return ls.pointerSchemas.get(absPointerString);
        }
        boolean isExternal = !absPointerString.startsWith("#");
        JSONPointer pointer = isExternal
                ? JSONPointer.forURL(ls.httpClient, absPointerString)
                : JSONPointer.forDocument(ls.rootSchemaJson, absPointerString);
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(relPointerString);
        ls.pointerSchemas.put(absPointerString, refBuilder);
        QueryResult result = pointer.query();
        JSONObject resultObject = extend(withoutRef(ctx), result.getQueryResult());
        SchemaLoader childLoader =
                selfBuilder().resolutionScope(isExternal ? withoutFragment(absPointerString) : id)
                        .schemaJson(resultObject)
                        .rootSchemaJson(result.getContainingDocument()).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

    private boolean schemaHasAnyOf(Collection<String> propNames) {
        return propNames.stream().filter(ls.schemaJson::has).findAny().isPresent();
    }

    private SchemaLoaderBuilder selfBuilder() {
        SchemaLoaderBuilder rval = builder().resolutionScope(ls.id).schemaJson(ls.schemaJson)
                .rootSchemaJson(ls.rootSchemaJson)
                .pointerSchemas(ls.pointerSchemas)
                .httpClient(ls.httpClient)
                .formatValidators(ls.formatValidators);
        return rval;
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

    private CombinedSchema.Builder tryCombinedSchema() {
        List<String> presentKeys = COMB_SCHEMA_PROVIDERS.keySet().stream()
                .filter(ls.schemaJson::has)
                .collect(Collectors.toList());
        if (presentKeys.size() > 1) {
            throw new SchemaException(String.format(
                    "expected at most 1 of 'allOf', 'anyOf', 'oneOf', %d found", presentKeys.size()));
        } else if (presentKeys.size() == 1) {
            String key = presentKeys.get(0);
            JSONArray subschemaDefs = ls.schemaJson.getJSONArray(key);
            Collection<Schema> subschemas = IntStream.range(0, subschemaDefs.length())
                    .mapToObj(subschemaDefs::getJSONObject)
                    .map(this::loadChild)
                    .map(Schema.Builder::build)
                    .collect(Collectors.toList());
            CombinedSchema.Builder combinedSchema = COMB_SCHEMA_PROVIDERS.get(key).apply(
                    subschemas);
            Schema.Builder<?> baseSchema;
            if (ls.schemaJson.has("type")) {
                baseSchema = loadForType(ls.schemaJson.get("type"));
            } else {
                baseSchema = sniffSchemaByProps();
            }
            if (baseSchema == null) {
                return combinedSchema;
            } else {
                return CombinedSchema.allOf(asList(baseSchema.build(), combinedSchema.build()));
            }
        } else {
            return null;
        }
    }


    /**
     * Rerurns a shallow copy of the {@code original} object, but it does not copy the {@code $ref}
     * key, in case it is present in {@code original}.
     */
    JSONObject withoutRef(JSONObject original) {
        String[] names = JSONObject.getNames(original);
        if (names == null) {
            return original;
        }
        JSONObject rval = new JSONObject();
        Arrays.stream(names)
                .filter(name -> !"$ref".equals(name))
                .forEach(name -> rval.put(name, original.get(name)));
        return rval;
    }
}
