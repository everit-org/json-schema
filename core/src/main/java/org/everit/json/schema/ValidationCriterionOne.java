package org.everit.json.schema;

import static java.lang.String.format;

public class ValidationCriterionOne implements ValidationCriterion {
    @Override
    public void validate(int subschemaCount, int matchingCount) {
        if (matchingCount != 1) {
            throw new ValidationException(null, format("%d subschemas matched instead of one",
                    matchingCount), "oneOf");
        }
    }

    @Override public String toString() {
        return "oneOf";
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ValidationCriterionOne;
    }

    @Override
    public int hashCode() {
        return ValidationCriterionOne.class.hashCode();
    }
}
