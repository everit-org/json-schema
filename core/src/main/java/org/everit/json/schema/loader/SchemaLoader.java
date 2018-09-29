package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_7;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.EmptySchema;
import org.everit.json.schema.FalseSchema;
import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.TrueSchema;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.everit.json.schema.loader.internal.WrappingFormatValidator;
import org.everit.json.schema.regexp.JavaUtilRegexpFactory;
import org.everit.json.schema.regexp.RegexpFactory;
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

        private boolean specVersionIsExplicitlySet = false;

        boolean useDefaults = false;

        private boolean nullableSupport = false;

        RegexpFactory regexpFactory = new JavaUtilRegexpFactory();

        public SchemaLoaderBuilder() {
            setSpecVersion(DRAFT_4);
        }

        /**
         * Registers a format validator with the name returned by {@link FormatValidator#formatName()}.
         *
         * @param formatValidator
         *         the format validator to be registered with its name
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
            specVersionIsExplicitlySet = true;
            return this;
        }

        public SchemaLoaderBuilder draftV7Support() {
            setSpecVersion(DRAFT_7);
            specVersionIsExplicitlySet = true;
            return this;
        }

        private void setSpecVersion(SpecificationVersion specVersion) {
            this.specVersion = specVersion;
        }

        private Optional<SpecificationVersion> specVersionInSchema() {
            Optional<SpecificationVersion> specVersion = Optional.empty();
            if (schemaJson instanceof Map) {
                Map<String, Object> schemaObj = (Map<String, Object>) schemaJson;
                String metaSchemaURL = (String) schemaObj.get("$schema");
                try {
                    specVersion = Optional.ofNullable(metaSchemaURL).map((SpecificationVersion::getByMetaSchemaUrl));
                } catch (IllegalArgumentException e) {
                    return specVersion;
                }
            }
            return specVersion;
        }

        public SchemaLoader build() {
            specVersionInSchema().ifPresent(this::setSpecVersion);
            formatValidators.putAll(specVersion.defaultFormatValidators());
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

        public SchemaLoaderBuilder regexpFactory(RegexpFactory regexpFactory) {
            this.regexpFactory = regexpFactory;
            return this;
        }
    }

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
        Object effectiveRootSchemaJson = builder.rootSchemaJson == null
                ? builder.schemaJson
                : builder.rootSchemaJson;
        Optional<String> schemaKeywordValue = extractSchemaKeywordValue(effectiveRootSchemaJson);
        SpecificationVersion specVersion;
        if (schemaKeywordValue.isPresent()) {
            try {
                specVersion = SpecificationVersion.getByMetaSchemaUrl(schemaKeywordValue.get());
            } catch (IllegalArgumentException e) {
                if (builder.specVersionIsExplicitlySet) {
                    specVersion = builder.specVersion;
                } else {
                    throw new SchemaException("#", "could not determine version");
                }
            }
        } else {
            specVersion = builder.specVersion;
        }
        this.config = new LoaderConfig(builder.httpClient,
                builder.formatValidators,
                specVersion,
                builder.useDefaults,
                builder.nullableSupport,
                builder.regexpFactory);
        this.ls = new LoadingState(config,
                builder.pointerSchemas,
                effectiveRootSchemaJson,
                builder.schemaJson,
                builder.id,
                builder.pointerToCurrentObj);
    }

    private static Optional<String> extractSchemaKeywordValue(Object effectiveRootSchemaJson) {
        if (effectiveRootSchemaJson instanceof Map) {
            Map<String, Object> schemaObj = (Map<String, Object>) effectiveRootSchemaJson;
            Object schemaValue = schemaObj.get("$schema");
            if (schemaValue != null) {
                return Optional.of((String) schemaValue);
            }
        }
        if (effectiveRootSchemaJson instanceof JsonObject) {
            JsonObject schemaObj = (JsonObject) effectiveRootSchemaJson;
            Object schemaValue = schemaObj.get("$schema");
            if (schemaValue != null) {
                return Optional.of((String) schemaValue);
            }
        }
        return Optional.empty();
    }

    SchemaLoader(LoadingState ls) {
        this.ls = ls;
        this.config = ls.config;
    }

    private Schema.Builder loadSchemaBoolean(Boolean rawBoolean) {
        return rawBoolean ? TrueSchema.builder() : FalseSchema.builder();
    }

    private Schema.Builder loadSchemaObject(JsonObject o) {
        Collection<Schema.Builder<?>> extractedSchemas = runSchemaExtractors(o);
        Schema.Builder effectiveReturnedSchema;
        if (extractedSchemas.isEmpty()) {
            effectiveReturnedSchema = EmptySchema.builder();
        } else if (extractedSchemas.size() == 1) {
            effectiveReturnedSchema = extractedSchemas.iterator().next();
        } else {
            Collection<Schema> built = extractedSchemas.stream()
                    .map(Schema.Builder::build)
                    .map(Schema.class::cast)
                    .collect(toList());
            effectiveReturnedSchema = CombinedSchema.allOf(built).isSynthetic(true);
        }
        loadCommonSchemaProperties(effectiveReturnedSchema);
        return effectiveReturnedSchema;
    }

    private Collection<Schema.Builder<?>> runSchemaExtractors(JsonObject o) {
        if (o.containsKey("$ref")) {
            return new ReferenceSchemaExtractor(this).extract(o).extractedSchemas;
        }
        Collection<Schema.Builder<?>> extractedSchemas;
        List<SchemaExtractor> extractors = asList(
                new EnumSchemaExtractor(this),
                new CombinedSchemaLoader(this),
                new NotSchemaExtractor(this),
                new ConstSchemaExtractor(this),
                new TypeBasedSchemaExtractor(this),
                new PropertySnifferSchemaExtractor(this)
        );
        AdjacentSchemaExtractionState state = new AdjacentSchemaExtractionState(o);
        for (SchemaExtractor extractor : extractors) {
            ExtractionResult result = extractor.extract(state.projectedSchemaJson());
            state = state.reduce(result);
        }
        extractedSchemas = state.extractedSchemaBuilders();
        return extractedSchemas;
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

    Schema.Builder<?> loadChild(JsonValue childJson) {
        return new SchemaLoader(childJson.ls).load();
    }

    SpecificationVersion specVersion() {
        return ls.specVersion();
    }

    /**
     * @param formatName
     * @return
     * @deprecated
     */
    @Deprecated
    Optional<FormatValidator> getFormatValidator(String formatName) {
        return Optional.ofNullable(config.formatValidators.get(formatName));
    }

}
