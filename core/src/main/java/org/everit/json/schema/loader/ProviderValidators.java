package org.everit.json.schema.loader;

import org.everit.json.schema.FormatValidator;
import java.util.Map;

/**
 * Interface for the provider of validators used in Schema
 */
public interface ProviderValidators {

    /**
     * Return a FormatValidator with a name
     *
     * @param formatName Name of the FormatValidator
     * @return
     */
    FormatValidator getFormatValidator(String formatName);

    /**
     * Add a FormatValidator with a name
     *
     * @param formatName  Name of the FormatValidator
     * @param formatValidator The FormatValidator to add
     */
    void addFormatValidator(String formatName, FormatValidator formatValidator);

    /**
     * Add a FormatValidator with a name if is or is not present
     *
     * @param formatName  Name of the FormatValidator
     * @param formatValidator The FormatValidator to add
     * @param addIfAbsent If true, the FormatValidator can add on map
     */
    void addFormatValidator(String formatName, FormatValidator formatValidator, boolean addIfAbsent);

    /**
     * Add a map of FormatValidator to the base map
     *
     * @param formatValidators All the FormatValidator to add on a base map
     */
    void addAllFormatValidators(Map<String, FormatValidator> formatValidators);

    /**
     * Initialize the map of FormatValidator
     *
     * @param formatValidators the map of FormatValidator
     */
    void initAllFormatValidators(Map<String, FormatValidator> formatValidators);

    /**
     * Return all the validators
     *
     * @return The map of all Validators
     */
    Map<String, FormatValidator> getFormatValidators();
}
