package org.everit.json.schema;

import static java.lang.String.format;

public class ValidationCriterionAny implements ValidationCriterion {

    @Override
    public void validate(int subschemaCount, int matchingCount) {
        if (matchingCount == 0) {
            throw new ValidationException(null, format(
                    "no subschema matched out of the total %d subschemas",
                    subschemaCount), "anyOf");
        }
    }

    @Override
    public String toString() {
        return "anyOf";
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ValidationCriterionAny;
    }

    @Override
    public int hashCode() {
        return ValidationCriterionAny.class.hashCode();
    }
}
