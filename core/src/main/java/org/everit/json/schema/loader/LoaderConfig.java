package org.everit.json.schema.loader;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.FormatValidator.v4Defaults;

/**
 * @author erosb
 */
class LoaderConfig {

    static LoaderConfig defaultV4Config() {
        return new LoaderConfig(new DefaultSchemaClient(), v4Defaults(), SpecificationVersion.DRAFT_4);
    }

    final SchemaClient httpClient;

    final Map<String, FormatValidator> formatValidators;

    final SpecificationVersion specVersion;

    LoaderConfig(SchemaClient httpClient, Map<String, FormatValidator> formatValidators,
            SpecificationVersion specVersion) {
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
        this.formatValidators = requireNonNull(formatValidators, "formatValidators cannot be null");
        this.specVersion = requireNonNull(specVersion, "specVersion cannot be null");
    }

}
