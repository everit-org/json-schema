package org.everit.json.schema;

/**
 * Validation criterion.
 */
@FunctionalInterface
public interface ValidationCriterion {

    /**
     * Throws a {@link ValidationException} if the implemented criterion is not fulfilled by the
     * {@code subschemaCount} and the {@code matchingSubschemaCount}.
     *
     * @param subschemaCount
     *         the total number of checked subschemas
     * @param matchingSubschemaCount
     *         the number of subschemas which successfully validated the subject (did not throw
     *         {@link ValidationException})
     */
    void validate(int subschemaCount, int matchingSubschemaCount);

}
