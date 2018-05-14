package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;

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

    final SchemaClient httpClient;

    final Map<String, FormatValidator> formatValidators;

    final SpecificationVersion specVersion;

    final boolean useDefaults;

    final boolean nullableSupport;

    final RegexpFactory regexpFactory;

    LoaderConfig(SchemaClient httpClient, Map<String, FormatValidator> formatValidators,
            SpecificationVersion specVersion, boolean useDefaults) {
        this(httpClient, formatValidators, specVersion, useDefaults, false, new JavaUtilRegexpFactory());
    }

    LoaderConfig(SchemaClient httpClient, Map<String, FormatValidator> formatValidators,
            SpecificationVersion specVersion, boolean useDefaults, boolean nullableSupport,
            RegexpFactory regexpFactory) {
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
        this.formatValidators = requireNonNull(formatValidators, "formatValidators cannot be null");
        this.specVersion = requireNonNull(specVersion, "specVersion cannot be null");
        this.useDefaults = useDefaults;
        this.nullableSupport = nullableSupport;
        this.regexpFactory = requireNonNull(regexpFactory, "regexpFactory cannot be null");
    }

}
