package org.everit.json.schema.loader;

import org.everit.json.schema.*;
import org.everit.json.schema.internal.*;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.everit.json.schema.loader.internal.WrappingFormatValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

/**
 * Loads a JSON schema's JSON representation into schema validator instances.
 */
public class SchemaLoader {

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
                id,
                new JSONPointer(""));
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

    private CombinedSchema.Builder buildAnyOfSchemaForMultipleTypes() {
        JSONArray subtypeJsons = ls.schemaJson.getJSONArray("type");
        Collection<Schema> subschemas = new ArrayList<>(subtypeJsons.length());
        for (int i = 0; i < subtypeJsons.length(); ++i) {
            String subtypeJson = subtypeJsons.getString(i);
            Schema.Builder<?> schemaBuilder = loadForExplicitType(subtypeJson);
            subschemas.add(schemaBuilder.build());
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

    private Schema.Builder<?> buildSchemaWithoutExplicitType() {
        if (ls.schemaJson.length() == 0) {
            return EmptySchema.builder();
        }
        if (ls.schemaJson.has("$ref")) {
            return new ReferenceLookup(ls).lookup(ls.schemaJson.getString("$ref"), ls.schemaJson);
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

    private NumberSchema.Builder buildNumberSchema() {
        NumberSchema.Builder builder = NumberSchema.builder();
        ls.ifPresent("minimum", Number.class, builder::minimum);
        ls.ifPresent("maximum", Number.class, builder::maximum);
        ls.ifPresent("multipleOf", Number.class, builder::multipleOf);
        ls.ifPresent("exclusiveMinimum", Boolean.class, builder::exclusiveMinimum);
        ls.ifPresent("exclusiveMaximum", Boolean.class, builder::exclusiveMaximum);
        return builder;
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
            builder = new CombinedSchemaLoader(ls, this).load()
                    .orElseGet(() -> {
                        if (!ls.schemaJson.has("type") || ls.schemaJson.has("$ref")) {
                            return buildSchemaWithoutExplicitType();
                        } else {
                            return loadForType(ls.schemaJson.get("type"));
                        }
                    });
        }
        ls.ifPresent("id", String.class, builder::id);
        ls.ifPresent("title", String.class, builder::title);
        ls.ifPresent("description", String.class, builder::description);
        return builder;
    }

    private Schema.Builder<?> loadForExplicitType(final String typeString) {
        switch (typeString) {
        case "string":
            return new StringSchemaLoader(ls).load();
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

    private ObjectSchema.Builder buildObjectSchema() {
        return new ObjectSchemaLoader(ls, this).load();
    }

    private ArraySchema.Builder buildArraySchema() {
        return new ArraySchemaLoader(ls, this).load();
    }

    Schema.Builder<?> loadForType(Object type) {
        if (type instanceof JSONArray) {
            return buildAnyOfSchemaForMultipleTypes();
        } else if (type instanceof String) {
            return loadForExplicitType((String) type);
        } else {
            throw new SchemaException("type", asList(JSONArray.class, String.class), type);
        }
    }

    private boolean schemaHasAnyOf(Collection<String> propNames) {
        return propNames.stream().filter(ls.schemaJson::has).findAny().isPresent();
    }

    Schema.Builder<?> loadChild(final JSONObject childJson) {
        return ls.initChildLoader().schemaJson(childJson).build().load();
    }

    Schema.Builder<?> sniffSchemaByProps() {
        if (schemaHasAnyOf(ARRAY_SCHEMA_PROPS)) {
            return buildArraySchema().requiresArray(false);
        } else if (schemaHasAnyOf(OBJECT_SCHEMA_PROPS)) {
            return buildObjectSchema().requiresObject(false);
        } else if (schemaHasAnyOf(NUMBER_SCHEMA_PROPS)) {
            return buildNumberSchema().requiresNumber(false);
        } else if (schemaHasAnyOf(STRING_SCHEMA_PROPS)) {
            return new StringSchemaLoader(ls).load().requiresString(false);
        }
        return null;
    }

    /**
     *
     * @param formatName
     * @return
     * @deprecated use {@link LoadingState#getFormatValidator(String)} instead.
     */
    @Deprecated Optional<FormatValidator> getFormatValidator(String formatName) {
        return ls.getFormatValidator(formatName);
    }

}
