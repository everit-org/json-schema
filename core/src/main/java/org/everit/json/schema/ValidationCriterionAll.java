package org.everit.json.schema;

import static java.lang.String.format;

public class ValidationCriterionAll implements ValidationCriterion {

    @Override
    public void validate(int subschemaCount, int matchingCount) {
        if (matchingCount < subschemaCount) {
            throw new ValidationException(null,
                    format("only %d subschema matches out of %d", matchingCount, subschemaCount),
                    "allOf"
            );
        }
    }

    @Override
    public String toString() {
        return "allOf";
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ValidationCriterionAll;
    }

    @Override
    public int hashCode() {
        return ValidationCriterionAll.class.hashCode();
    }
}
