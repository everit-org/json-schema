package org.everit.json.schema.loader;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.everit.json.schema.regexp.JavaUtilRegexpFactory;
import org.everit.json.schema.regexp.RegexpFactory;

/**
 * @author erosb
 */
class LoaderConfig {

    static LoaderConfig defaultV4Config() {
        return new LoaderConfig(new DefaultSchemaClient(), DRAFT_4.defaultFormatValidators(), DRAFT_4, false);
    }

    final SchemaClient schemaClient;

    final Map<String, FormatValidator> formatValidators;

    final Map<URI, Object> schemasByURI;

    final SpecificationVersion specVersion;

    final boolean useDefaults;

    final boolean nullableSupport;

    final RegexpFactory regexpFactory;

    LoaderConfig(SchemaClient schemaClient, Map<String, FormatValidator> formatValidators,
            SpecificationVersion specVersion, boolean useDefaults) {
        this(schemaClient, formatValidators, emptyMap(), specVersion, useDefaults, false, new JavaUtilRegexpFactory());
    }

    LoaderConfig(SchemaClient schemaClient, Map<String, FormatValidator> formatValidators,
            Map<URI, Object> schemasByURI,
            SpecificationVersion specVersion, boolean useDefaults, boolean nullableSupport,
            RegexpFactory regexpFactory) {
        this.schemaClient = requireNonNull(schemaClient, "schemaClient cannot be null");
        this.formatValidators = requireNonNull(formatValidators, "formatValidators cannot be null");
        if (schemasByURI == null) {
            this.schemasByURI = new HashMap<>();
        } else {
            this.schemasByURI = schemasByURI;
        }
        this.specVersion = requireNonNull(specVersion, "specVersion cannot be null");
        this.useDefaults = useDefaults;
        this.nullableSupport = nullableSupport;
        this.regexpFactory = requireNonNull(regexpFactory, "regexpFactory cannot be null");
    }

}
