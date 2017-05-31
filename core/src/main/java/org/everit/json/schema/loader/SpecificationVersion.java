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

    }, DRAFT_6 {
        @Override List<String> arrayKeywords() {
            return asList("items", "additionalItems", "minItems",
                    "maxItems", "uniqueItems", "contains");
        }
    };

    abstract List<String> arrayKeywords();

}
