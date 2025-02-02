package org.everit.json.schema.loader.internal;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.loader.ProviderValidators;
import org.everit.json.schema.loader.SchemaClient;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *  The Default {@link ProviderValidators} implementation which useful for collect all default and custom validators
 */
public class DefaultProviderValidators implements ProviderValidators {

    Map<String, FormatValidator> formatValidators = new HashMap<>();

    public DefaultProviderValidators() { }

    public DefaultProviderValidators(Map<String, FormatValidator> formatValidators) {
        this.initAllFormatValidators(formatValidators);
    }

    @Override
    public FormatValidator getFormatValidator(String formatName) {
        return this.formatValidators.get(formatName);
    }

    @Override
    public void addFormatValidator(String formatName, FormatValidator formatValidator) {
        this.formatValidators.put(formatName, formatValidator);
    }

    @Override
    public void addFormatValidator(String formatName, FormatValidator formatValidator, boolean addIfAbsent) {
        if (addIfAbsent) {
            this.formatValidators.putIfAbsent(formatName, formatValidator);
        } else {
            this.addFormatValidator(formatName, formatValidator);
        }
    }

    @Override
    public void addAllFormatValidators(Map<String, FormatValidator> formatValidators) {
        this.formatValidators.putAll(formatValidators);
    }

    @Override
    public void initAllFormatValidators(Map<String, FormatValidator> formatValidators) {
        this.formatValidators = formatValidators;
    }

    @Override
    public Map<String, FormatValidator> getFormatValidators() {
        return formatValidators;
    }
}
