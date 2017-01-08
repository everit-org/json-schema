package org.everit.json.schema.loader;

/**
 * @author erosb
 */
@FunctionalInterface
interface JsonObjectIterator {

    void apply(String key, JsonValue value);

}
