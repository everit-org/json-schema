package org.everit.json.schema.loader;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.collections.ListUtils.unmodifiableList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.internal.DateTimeFormatValidator;
import org.everit.json.schema.internal.EmailFormatValidator;
import org.everit.json.schema.internal.HostnameFormatValidator;
import org.everit.json.schema.internal.IPV4Validator;
import org.everit.json.schema.internal.IPV6Validator;
import org.everit.json.schema.internal.URIFormatValidator;

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
            Map<String, FormatValidator> formatValidators = new HashMap<>();
            formatValidators.put("date-time", new DateTimeFormatValidator());
            formatValidators.put("uri", new URIFormatValidator());
            formatValidators.put("email", new EmailFormatValidator());
            formatValidators.put("ipv4", new IPV4Validator());
            formatValidators.put("ipv6", new IPV6Validator());
            formatValidators.put("hostname", new HostnameFormatValidator());
            return unmodifiableMap(formatValidators);
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
            throw new UnsupportedOperationException();
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

    abstract List<String> arrayKeywords();

    abstract List<String> objectKeywords();

    abstract String idKeyword();

    abstract String metaSchemaUrl();

    abstract Map<String, FormatValidator> defaultFormatValidators();

}
