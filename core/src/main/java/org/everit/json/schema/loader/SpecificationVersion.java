package org.everit.json.schema.loader;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.internal.DateFormatValidator;
import org.everit.json.schema.internal.DateTimeFormatValidator;
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

/**
 * @author erosb
 */
enum SpecificationVersion {

    DRAFT_4 {
        @Override List<String> arrayKeywords() {
            return V4_ARRAY_KEYWORDS;
        }

        @Override List<String> objectKeywords() {
            return V4_OBJECT_KEYWORDS;
        }

        @Override String idKeyword() {
            return "id";
        }

        @Override String metaSchemaUrl() {
            return "http://json-schema.org/draft-04/schema";
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

        @Override String idKeyword() {
            return "$id";
        }

        @Override String metaSchemaUrl() {
            return "http://json-schema.org/draft-06/schema";
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

        @Override String idKeyword() {
            return DRAFT_6.idKeyword();
        }

        @Override String metaSchemaUrl() {
            return "http://json-schema.org/draft-07/schema";
        }

        @Override Map<String, FormatValidator> defaultFormatValidators() {
            return V7_VALIDATORS;
        }
    };

    static SpecificationVersion getByMetaSchemaUrl(String metaSchemaUrl) {
        return Arrays.stream(values())
                .filter(v -> metaSchemaUrl.startsWith(v.metaSchemaUrl()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        format("could not determine schema version: no meta-schema is known with URL [%s]", metaSchemaUrl)
                ));
    }

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

    private static final List<String> keywords(String... keywords) {
        return unmodifiableList(asList(keywords));
    }

    private static final Map<String, FormatValidator> V4_VALIDATORS;

    static {
        Map<String, FormatValidator> formatValidators = new HashMap<>();
        formatValidators.put("date-time", new DateTimeFormatValidator());
        formatValidators.put("uri", new URIV4FormatValidator());
        formatValidators.put("email", new EmailFormatValidator());
        formatValidators.put("ipv4", new IPV4Validator());
        formatValidators.put("ipv6", new IPV6Validator());
        formatValidators.put("hostname", new HostnameFormatValidator());
        V4_VALIDATORS = unmodifiableMap(formatValidators);
    }

    private static final Map<String, FormatValidator> V6_VALIDATORS;

    static {
        Map<String, FormatValidator> v6Validators = new HashMap<>(V4_VALIDATORS);
        v6Validators.put("json-pointer", new JsonPointerFormatValidator());
        v6Validators.put("uri", new URIFormatValidator());
        v6Validators.put("uri-reference", new URIReferenceFormatValidator());
        v6Validators.put("uri-template", new URITemplateFormatValidator());
        V6_VALIDATORS = unmodifiableMap(v6Validators);
    }

    private static final Map<String, FormatValidator> V7_VALIDATORS;

    static {
        Map<String, FormatValidator> formatValidators = new HashMap<>(V6_VALIDATORS);
        formatValidators.put("date", new DateFormatValidator());
        formatValidators.put("uri", new URIFormatValidator(false));
        formatValidators.put("time", new TimeFormatValidator());
        formatValidators.put("regex", new RegexFormatValidator());
        formatValidators.put("relative-json-pointer", new RelativeJsonPointerFormatValidator());
        V7_VALIDATORS = unmodifiableMap(formatValidators);
    }

    abstract List<String> arrayKeywords();

    abstract List<String> objectKeywords();

    abstract String idKeyword();

    abstract String metaSchemaUrl();

    abstract Map<String, FormatValidator> defaultFormatValidators();

    public boolean isAtLeast(SpecificationVersion lowerInclusiveBound) {
        return this.ordinal() >= lowerInclusiveBound.ordinal();
    }
}
