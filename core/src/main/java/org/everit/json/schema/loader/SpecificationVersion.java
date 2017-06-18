package org.everit.json.schema.loader;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.ListUtils.unmodifiableList;

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

}
