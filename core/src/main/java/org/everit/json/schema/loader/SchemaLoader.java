package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_7;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ConditionalSchema;
import org.everit.json.schema.ConstSchema;
import org.everit.json.schema.EmptySchema;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.FalseSchema;
import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.NotSchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.TrueSchema;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.everit.json.schema.loader.internal.WrappingFormatValidator;
import org.json.JSONObject;
import org.json.JSONPointer;

/**
 * Loads a JSON schema's JSON representation into schema validator instances.
 */
public class SchemaLoader {

    static JSONObject toOrgJSONObject(JsonObject value) {
        return new JSONObject(value.toMap());
    }

    /**
     * Builder class for {@link SchemaLoader}.
     */
    public static class SchemaLoaderBuilder {

        SchemaClient httpClient = new DefaultSchemaClient();

        Object schemaJson;

        Object rootSchemaJson;

        Map<String, ReferenceSchema.Builder> pointerSchemas = new HashMap<>();

        URI id;

        List<String> pointerToCurrentObj = emptyList();

        Map<String, FormatValidator> formatValidators = new HashMap<>();

        SpecificationVersion specVersion;

        boolean useDefaults = false;

        private boolean nullableSupport = false;

        public SchemaLoaderBuilder() {
            setSpecVersion(DRAFT_4);
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
         * @param formatName
         *         the name which will be used in the schema JSON files to refer to this {@code formatValidator}
         * @param formatValidator
         *         the object performing the validation for schemas which use the {@code formatName} format
         * @return {@code this}
         * @deprecated instead it is better to override {@link FormatValidator#formatName()}
         * and use {@link #addFormatValidator(FormatValidator)}
         */
        @Deprecated
        public SchemaLoaderBuilder addFormatValidator(String formatName,
                final FormatValidator formatValidator) {
            if (!Objects.equals(formatName, formatValidator.formatName())) {
                formatValidators.put(formatName, new WrappingFormatValidator(formatName, formatValidator));
            } else {
                formatValidators.put(formatName, formatValidator);
            }
            return this;
        }

        public SchemaLoaderBuilder draftV6Support() {
            setSpecVersion(DRAFT_6);
            return this;
        }

        public SchemaLoaderBuilder draftV7Support() {
            setSpecVersion(DRAFT_7);
            return this;
        }

        private void setSpecVersion(SpecificationVersion specVersion) {
            this.specVersion = specVersion;
            specVersion.defaultFormatValidators().forEach(this::addFormatValidator);
            //            this.formatValidators = new HashMap<>(specVersion.defaultFormatValidators());
        }

        public SchemaLoader build() {
            return new SchemaLoader(this);
        }

        @Deprecated
        public JSONObject getRootSchemaJson() {
            return new JSONObject((Map<String, Object>) (rootSchemaJson == null ? schemaJson : rootSchemaJson));
        }

        public SchemaLoaderBuilder httpClient(SchemaClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Sets the initial resolution scope of the schema. {@code id} and {@code $ref} attributes
         * accuring in the schema will be resolved against this value.
         *
         * @param id
         *         the initial (absolute) URI, used as the resolution scope.
         * @return {@code this}
         */
        public SchemaLoaderBuilder resolutionScope(String id) {
            try {
                return resolutionScope(new URI(id));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        public SchemaLoaderBuilder resolutionScope(URI id) {
            this.id = id;
            return this;
        }

        SchemaLoaderBuilder pointerSchemas(Map<String, ReferenceSchema.Builder> pointerSchemas) {
            this.pointerSchemas = pointerSchemas;
            return this;
        }

        SchemaLoaderBuilder rootSchemaJson(Object rootSchemaJson) {
            this.rootSchemaJson = rootSchemaJson;
            return this;
        }

        public SchemaLoaderBuilder schemaJson(JSONObject schemaJson) {
            return schemaJson(schemaJson.toMap());
        }

        public SchemaLoaderBuilder schemaJson(Object schema) {
            if (schema instanceof JSONObject) {
                schema = (((JSONObject) schema).toMap());
            }
            this.schemaJson = schema;
            return this;
        }

        SchemaLoaderBuilder formatValidators(Map<String, FormatValidator> formatValidators) {
            this.formatValidators = formatValidators;
            return this;
        }

        SchemaLoaderBuilder pointerToCurrentObj(List<String> pointerToCurrentObj) {
            this.pointerToCurrentObj = requireNonNull(pointerToCurrentObj);
            return this;
        }

        /**
         * With this flag set to false, the validator ignores the default keyword inside the json schema.
         * If is true, validator applies default values when it's needed
         *
         * @param useDefaults
         *         if true, validator doesn't ignore default values
         * @return {@code this}
         */
        public SchemaLoaderBuilder useDefaults(boolean useDefaults) {
            this.useDefaults = useDefaults;
            return this;
        }

        public SchemaLoaderBuilder nullableSupport(boolean nullableSupport) {
            this.nullableSupport = nullableSupport;
            return this;
        }
    }

    private static final List<String> CONDITIONAL_SCHEMA_KEYWORDS = asList("if", "then", "else");

    private static final List<String> NUMBER_SCHEMA_PROPS = asList("minimum", "maximum",
            "exclusiveMinimum", "exclusiveMaximum", "multipleOf");

    private static final List<String> STRING_SCHEMA_PROPS = asList("minLength", "maxLength",
            "pattern", "format");

    public static SchemaLoaderBuilder builder() {
        return new SchemaLoaderBuilder();
    }

    /**
     * Loads a JSON schema to a schema validator using a {@link DefaultSchemaClient default HTTP
     * client}.
     *
     * @param schemaJson
     *         the JSON representation of the schema.
     * @return the schema validator object
     */
    public static Schema load(final JSONObject schemaJson) {
        return SchemaLoader.load(schemaJson, new DefaultSchemaClient());
    }

    /**
     * Creates Schema instance from its JSON representation.
     *
     * @param schemaJson
     *         the JSON representation of the schema.
     * @param httpClient
     *         the HTTP client to be used for resolving remote JSON references.
     * @return the created schema
     */
    public static Schema load(final JSONObject schemaJson, final SchemaClient httpClient) {
        SchemaLoader loader = builder()
                .schemaJson(schemaJson)
                .httpClient(httpClient)
                .build();
        return loader.load().build();
    }

    private final LoaderConfig config;

    private final LoadingState ls;

    private final ExclusiveLimitHandler exclusiveLimitHandler;

    /**
     * Constructor.
     *
     * @param builder
     *         the builder containing the properties. Only {@link SchemaLoaderBuilder#id} is
     *         nullable.
     * @throws NullPointerException
     *         if any of the builder properties except {@link SchemaLoaderBuilder#id id} is
     *         {@code null}.
     */
    public SchemaLoader(SchemaLoaderBuilder builder) {
        SpecificationVersion specVersion = builder.specVersion;
        if (builder.schemaJson instanceof Map) {
            Map<String, Object> schemaObj = (Map<String, Object>) builder.schemaJson;
            Object schemaValue = schemaObj.get("$schema");
            if (schemaValue != null) {
                specVersion = SpecificationVersion.getByMetaSchemaUrl((String) schemaValue);
            }
        }
        this.config = new LoaderConfig(builder.httpClient,
                builder.formatValidators,
                specVersion,
                builder.useDefaults,
                builder.nullableSupport);
        this.ls = new LoadingState(config,
                builder.pointerSchemas,
                builder.rootSchemaJson == null ? builder.schemaJson : builder.rootSchemaJson,
                builder.schemaJson,
                builder.id,
                builder.pointerToCurrentObj);
        this.exclusiveLimitHandler = ExclusiveLimitHandler.ofSpecVersion(config.specVersion);
    }

    SchemaLoader(LoadingState ls) {
        this.ls = ls;
        this.config = ls.config;
        this.exclusiveLimitHandler = ExclusiveLimitHandler.ofSpecVersion(ls.specVersion());
    }

    private CombinedSchema.Builder buildAnyOfSchemaForMultipleTypes() {
        JsonArray subtypeJsons = ls.schemaJson().require("type").requireArray();
        Collection<Schema> subschemas = new ArrayList<>(subtypeJsons.length());
        subtypeJsons.forEach((j, raw) -> {
            subschemas.add(loadForExplicitType(raw.requireString()).build());
        });
        return CombinedSchema.anyOf(subschemas);
    }

    private Schema.Builder buildConstSchema() {
        return ConstSchema.builder()
                .permittedValue(ls.schemaJson().require("const").unwrap());
    }

    private EnumSchema.Builder buildEnumSchema() {
        EnumSchema.Builder builder = EnumSchema.builder();
        Set<Object> possibleValues = new HashSet<>();
        ls.schemaJson().require("enum").requireArray().forEach((i, item) -> possibleValues.add(item.unwrap()));
        builder.possibleValues(possibleValues);
        return builder;
    }

    private NotSchema.Builder buildNotSchema() {
        Schema mustNotMatch = loadChild(ls.schemaJson().require("not")).build();
        return NotSchema.builder().mustNotMatch(mustNotMatch);
    }

    private Schema.Builder<?> buildSchemaWithoutExplicitType() {
        if (ls.schemaJson().isEmpty()) {
            return EmptySchema.builder();
        }
        if (ls.schemaJson().containsKey("$ref")) {
            String ref = ls.schemaJson().require("$ref").requireString();
            return new ReferenceLookup(ls).lookup(ref, ls.schemaJson());
        }
        Schema.Builder<?> rval = sniffSchemaByProps();
        if (rval != null) {
            return rval;
        }
        if (ls.schemaJson().containsKey("not")) {
            return buildNotSchema();
        }
        return EmptySchema.builder();
    }

    private NumberSchema.Builder buildNumberSchema() {
        NumberSchema.Builder builder = NumberSchema.builder();
        ls.schemaJson().maybe("minimum").map(JsonValue::requireNumber).ifPresent(builder::minimum);
        ls.schemaJson().maybe("maximum").map(JsonValue::requireNumber).ifPresent(builder::maximum);
        ls.schemaJson().maybe("multipleOf").map(JsonValue::requireNumber).ifPresent(builder::multipleOf);
        ls.schemaJson().maybe("exclusiveMinimum")
                .ifPresent(exclMin -> exclusiveLimitHandler.handleExclusiveMinimum(exclMin, builder));
        ls.schemaJson().maybe("exclusiveMaximum")
                .ifPresent(exclMax -> exclusiveLimitHandler.handleExclusiveMaximum(exclMax, builder));
        return builder;
    }

    private ConditionalSchema.Builder buildConditionalSchema() {
        ConditionalSchema.Builder builder = ConditionalSchema.builder();
        ls.schemaJson().maybe("if").map(this::loadChild).map(Schema.Builder::build).ifPresent(builder::ifSchema);
        ls.schemaJson().maybe("then").map(this::loadChild).map(Schema.Builder::build).ifPresent(builder::thenSchema);
        ls.schemaJson().maybe("else").map(this::loadChild).map(Schema.Builder::build).ifPresent(builder::elseSchema);
        return builder;
    }

    private Schema.Builder loadSchemaBoolean(Boolean rawBoolean) {
        return rawBoolean ? TrueSchema.builder() : FalseSchema.builder();
    }

    private Schema.Builder loadSchemaObject(JsonObject o) {
        Schema.Builder builder;
        if (ls.schemaJson().containsKey("enum")) {
            builder = buildEnumSchema();
        } else if (ls.schemaJson().containsKey("const") && (config.specVersion != DRAFT_4)) {
            builder = buildConstSchema();
        } else {
            builder = new CombinedSchemaLoader(ls, this).load()
                    .orElseGet(() -> {
                        if (!ls.schemaJson().containsKey("type") || ls.schemaJson().containsKey("$ref")) {
                            return buildSchemaWithoutExplicitType();
                        } else {
                            return loadForType(ls.schemaJson().require("type"));
                        }
                    });
        }
        loadCommonSchemaProperties(builder);
        return builder;
    }

    private void loadCommonSchemaProperties(Schema.Builder builder) {
        ls.schemaJson().maybe(config.specVersion.idKeyword()).map(JsonValue::requireString).ifPresent(builder::id);
        ls.schemaJson().maybe("title").map(JsonValue::requireString).ifPresent(builder::title);
        ls.schemaJson().maybe("description").map(JsonValue::requireString).ifPresent(builder::description);
        if (ls.specVersion() == DRAFT_7) {
            ls.schemaJson().maybe("readOnly").map(JsonValue::requireBoolean).ifPresent(builder::readOnly);
            ls.schemaJson().maybe("writeOnly").map(JsonValue::requireBoolean).ifPresent(builder::writeOnly);
        }
        if (config.nullableSupport) {
            builder.nullable(ls.schemaJson()
                    .maybe("nullable")
                    .map(JsonValue::requireBoolean)
                    .orElse(Boolean.FALSE));
        }
        if (config.useDefaults) {
            ls.schemaJson().maybe("default").map(JsonValue::deepToOrgJson).ifPresent(builder::defaultValue);
        }
        builder.schemaLocation(new JSONPointer(ls.pointerToCurrentObj).toURIFragment());
    }

    /**
     * Populates a {@code Schema.Builder} instance from the {@code schemaJson} schema definition.
     *
     * @return the builder which already contains the validation criteria of the schema, therefore
     * {@link Schema.Builder#build()} can be immediately used to acquire the {@link Schema}
     * instance to be used for validation
     */
    public Schema.Builder<?> load() {
        return ls.schemaJson
                .canBeMappedTo(Boolean.class, this::loadSchemaBoolean)
                .orMappedTo(JsonObject.class, this::loadSchemaObject)
                .requireAny();
    }

    private Schema.Builder<?> loadForExplicitType(final String typeString) {
        switch (typeString) {
        case "string":
            return new StringSchemaLoader(ls, config.formatValidators).load();
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
        return new ObjectSchemaLoader(ls, this.config, this).load();
    }

    private ArraySchema.Builder buildArraySchema() {
        return new ArraySchemaLoader(ls, config, this).load();
    }

    Schema.Builder loadForType(JsonValue type) {
        return type.canBeMappedTo(JsonArray.class, arr -> (Schema.Builder) buildAnyOfSchemaForMultipleTypes())
                .orMappedTo(String.class, this::loadForExplicitType)
                .requireAny();
    }

    private boolean schemaHasAnyOf(Collection<String> propNames) {
        return propNames.stream().filter(ls.schemaJson()::containsKey).findAny().isPresent();
    }

    Schema.Builder<?> loadChild(JsonValue childJson) {
        return new SchemaLoader(childJson.ls).load();
    }

    Schema.Builder<?> sniffSchemaByProps() {
        if (schemaHasAnyOf(config.specVersion.arrayKeywords())) {
            return buildArraySchema().requiresArray(false);
        } else if (schemaHasAnyOf(config.specVersion.objectKeywords())) {
            return buildObjectSchema().requiresObject(false);
        } else if (schemaHasAnyOf(NUMBER_SCHEMA_PROPS)) {
            return buildNumberSchema().requiresNumber(false);
        } else if (schemaHasAnyOf(STRING_SCHEMA_PROPS)) {
            return new StringSchemaLoader(ls, config.formatValidators).load().requiresString(false);
        } else if (config.specVersion.isAtLeast(DRAFT_7) && schemaHasAnyOf(CONDITIONAL_SCHEMA_KEYWORDS)) {
            return buildConditionalSchema();
        }
        return null;
    }

    SpecificationVersion specVersion() {
        return ls.specVersion();
    }

    /**
     * @param formatName
     * @return
     * @deprecated
     */
    @Deprecated Optional<FormatValidator> getFormatValidator(String formatName) {
        return Optional.ofNullable(config.formatValidators.get(formatName));
    }

}
