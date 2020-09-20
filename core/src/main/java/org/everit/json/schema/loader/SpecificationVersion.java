package org.everit.json.schema.loader;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.internal.DateFormatValidator;
import org.everit.json.schema.internal.DateTimeFormatValidator;
import org.everit.json.schema.internal.DurationFormatValidator;
import org.everit.json.schema.internal.EmailFormatValidator;
import org.everit.json.schema.internal.HostnameFormatValidator;
import org.everit.json.schema.internal.IPV4Validator;
import org.everit.json.schema.internal.IPV6Validator;
import org.everit.json.schema.internal.JsonPointerFormatValidator;
import org.everit.json.schema.internal.RegexFormatValidator;
import org.everit.json.schema.internal.RelativeJsonPointerFormatValidator;
import org.everit.json.schema.internal.TimeFormatValidator;
import org.everit.json.schema.internal.URIFormatValidator;
import org.everit.json.schema.internal.URIReferenceFormatValidator;
import org.everit.json.schema.internal.URITemplateFormatValidator;
import org.everit.json.schema.internal.URIV4FormatValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * @author erosb
 */
public enum SpecificationVersion {

    DRAFT_4 {
        @Override List<String> arrayKeywords() {
            return V4_ARRAY_KEYWORDS;
        }

        @Override List<String> objectKeywords() {
            return V4_OBJECT_KEYWORDS;
        }

        @Override public String idKeyword() {
            return "id";
        }

        @Override
        public String dependentRequiredKeyword() {
            return "dependencies";
        }

        @Override List<String> metaSchemaUrls() {
            return Arrays.asList(
                "http://json-schema.org/draft-04/schema",
                "https://json-schema.org/draft-04/schema"
            );
        }

        @Override Map<String, FormatValidator> defaultFormatValidators() {
            return V4_VALIDATORS;
        }

    }, DRAFT_6 {
        @Override List<String> arrayKeywords() {
            return V6_ARRAY_KEYWORDS;
        }

        @Override List<String> objectKeywords() {
            return V6_OBJECT_KEYWORDS;
        }

        @Override public String idKeyword() {
            return "$id";
        }

        @Override
        public String dependentRequiredKeyword() {
            return DRAFT_4.dependentRequiredKeyword();
        }

        @Override List<String> metaSchemaUrls() {
            return Arrays.asList(
                "http://json-schema.org/draft-06/schema",
                "https://json-schema.org/draft-06/schema"
            );
        }

        @Override Map<String, FormatValidator> defaultFormatValidators() {
            return V6_VALIDATORS;
        }

    }, DRAFT_7 {
        @Override List<String> arrayKeywords() {
            return V6_ARRAY_KEYWORDS;
        }

        @Override List<String> objectKeywords() {
            return V6_OBJECT_KEYWORDS;
        }

        @Override public String idKeyword() {
            return DRAFT_6.idKeyword();
        }

        @Override
        public String dependentRequiredKeyword() {
            return DRAFT_4.dependentRequiredKeyword();
        }

        @Override List<String> metaSchemaUrls() {
            return Arrays.asList(
                "http://json-schema.org/draft-07/schema",
                "https://json-schema.org/draft-07/schema"
            );
        }

        @Override Map<String, FormatValidator> defaultFormatValidators() {
            return V7_VALIDATORS;
        }

    }, DRAFT_201909 {
        @Override List<String> arrayKeywords() {
            return V2019_ARRAY_KEYWORDS;
        }

        @Override List<String> objectKeywords() {
            return V2019_OBJECT_KEYWORDS;
        }

        @Override public String idKeyword() {
            return DRAFT_6.idKeyword();
        }

        @Override
        public String dependentRequiredKeyword() {
            return "dependentRequired";
        }

        @Override List<String> metaSchemaUrls() {
            return Arrays.asList(
                "http://json-schema.org/draft/2019-09/schema",
                "https://json-schema.org/draft/2019-09/schema"
            );
        }

        @Override Map<String, FormatValidator> defaultFormatValidators() {
            return V201909_VALIDATORS;
        }
    };

    static SpecificationVersion getByMetaSchemaUrl(String metaSchemaUrl) {
        return lookupByMetaSchemaUrl(metaSchemaUrl)
                .orElseThrow(() -> new IllegalArgumentException(
                        format("could not determine schema version: no meta-schema is known with URL [%s]", metaSchemaUrl)
                ));
    }

    public static Optional<SpecificationVersion> lookupByMetaSchemaUrl(String metaSchemaUrl) {
        return Arrays.stream(values())
                .filter(v -> v.metaSchemaUrls().stream().anyMatch(metaSchemaUrl::startsWith))
                .findFirst();
    }

    private static final List<String> V2019_OBJECT_KEYWORDS = keywords("properties", "required",
            "minProperties",
            "maxProperties",
            "dependentRequired",
            "patternProperties",
            "additionalProperties",
            "propertyNames",
            "dependentSchemas");

    private static final List<String> V2019_ARRAY_KEYWORDS = keywords("items", "additionalItems", "minItems",
            "maxItems", "uniqueItems", "contains", "unevaluatedItems");

    private static final List<String> V6_OBJECT_KEYWORDS = keywords("properties", "required",
            "minProperties",
            "maxProperties",
            "dependencies",
            "patternProperties",
            "additionalProperties",
            "propertyNames");

    private static final List<String> V6_ARRAY_KEYWORDS = keywords("items", "additionalItems", "minItems",
            "maxItems", "uniqueItems", "contains");

    private static final List<String> V4_OBJECT_KEYWORDS = keywords("properties", "required",
            "minProperties",
            "maxProperties",
            "dependencies",
            "patternProperties",
            "additionalProperties");

    private static final List<String> V4_ARRAY_KEYWORDS = keywords("items", "additionalItems", "minItems",
            "maxItems", "uniqueItems");

    private static List<String> keywords(String... keywords) {
        return unmodifiableList(asList(keywords));
    }

    private static final Map<String, FormatValidator> V4_VALIDATORS = formatValidators(null,
            new DateTimeFormatValidator(),
            new URIV4FormatValidator(),
            new EmailFormatValidator(),
            new IPV4Validator(),
            new IPV6Validator(),
            new HostnameFormatValidator()
    );

    private static final Map<String, FormatValidator> V6_VALIDATORS = formatValidators(V4_VALIDATORS,
            new JsonPointerFormatValidator(),
            new URIFormatValidator(),
            new URIReferenceFormatValidator(),
            new URITemplateFormatValidator()
    );

    private static final Map<String, FormatValidator> V7_VALIDATORS = formatValidators(V6_VALIDATORS,
            new DateFormatValidator(),
            new URIFormatValidator(false),
            new TimeFormatValidator(),
            new RegexFormatValidator(),
            new RelativeJsonPointerFormatValidator()
    );

    private static final Map<String, FormatValidator> V201909_VALIDATORS = formatValidators(V7_VALIDATORS,
            new DurationFormatValidator()
    );

    private static Map<String, FormatValidator> formatValidators(Map<String, FormatValidator> parent, FormatValidator... validators) {
        Map<String, FormatValidator> validatorMap = (parent == null) ? new HashMap<>() : new HashMap<>(parent);
        for (FormatValidator validator : validators) {
            validatorMap.put(validator.formatName(), validator);
        }
        return unmodifiableMap(validatorMap);
    }

    abstract List<String> arrayKeywords();

    abstract List<String> objectKeywords();

    public abstract String idKeyword();

    public abstract String dependentRequiredKeyword();

    abstract List<String> metaSchemaUrls();

    abstract Map<String, FormatValidator> defaultFormatValidators();

    public boolean isAtLeast(SpecificationVersion lowerInclusiveBound) {
        return this.ordinal() >= lowerInclusiveBound.ordinal();
    }
}
