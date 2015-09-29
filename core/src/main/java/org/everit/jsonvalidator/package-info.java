package org.everit.jsonvalidator;

/**
 * This package contains classes representing different kinds of JSON schema.
 *
 * <p>The base class for each schema-type implementation is the {@link org.everit.jsonvalidator.Schema} class. In most cases schema instances
 * are created via {@link org.everit.jsonvalidator.loader.SchemaLoader#load(org.json.JSONObject)} invocations, although direct schema
 * instantiations - therefore programmatic schema creation - is also possible.</p>
 *
 * <p>Every {@code Schema} subclass has its separate {@code Builder} class which permits the creation of schemas by chained method calls.
 */
