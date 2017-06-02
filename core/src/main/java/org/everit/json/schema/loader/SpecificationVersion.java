package org.everit.json.schema.loader;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author erosb
 */
enum SpecificationVersion {

    DRAFT_4 {

        @Override List<String> arrayKeywords() {
            return asList("items", "additionalItems", "minItems",
                    "maxItems", "uniqueItems");
        }

        @Override String idKeyword() {
            return "id";
        }

    }, DRAFT_6 {
        @Override List<String> arrayKeywords() {
            return asList("items", "additionalItems", "minItems",
                    "maxItems", "uniqueItems", "contains");
        }

        @Override String idKeyword() {
            return "$id";
        }

    };

    abstract List<String> arrayKeywords();

    abstract String idKeyword();

}
