package org.everit.json.schema.loader;

import org.everit.json.schema.FormatValidator;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;

/**
 * @author erosb
 */
class LoaderConfig {

    final SchemaClient httpClient;

    final Map<String, FormatValidator> formatValidators;

    final SpecificationVersion specVersion;

    public LoaderConfig(SchemaClient httpClient, Map<String, FormatValidator> formatValidators,
            SpecificationVersion specVersion) {
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
        this.formatValidators = requireNonNull(formatValidators, "formatValidators cannot be null");
        this.specVersion = requireNonNull(specVersion, "specVersion cannot be null");
    }

}
