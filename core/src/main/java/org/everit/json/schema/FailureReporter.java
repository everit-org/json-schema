package org.everit.json.schema;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

class FailureReporter {

    private List<ValidationException> failures = new ArrayList<>(1);

    private Schema schema;

    FailureReporter(Schema schema) {
        this.schema = requireNonNull(schema, "schema cannot be null");
    }

    void failure(String message, String keyword) {
        failures.add(new ValidationException(schema, message, keyword, schema.getSchemaLocation()));
    }

    void failure(Class<?> expectedType, Object actualValue) {
        failures.add(new ValidationException(schema, expectedType, actualValue, "type", schema.getSchemaLocation()));
    }

    void failure(ValidationException exc) {
        failures.add(exc);
    }

    void throwExceptionIfFailureFound() {
        ValidationException.throwFor(schema, failures);
    }

    ValidationException inContextOfSchema(Schema schema, Runnable task) {
        requireNonNull(schema, "schema cannot be null");
        int failureCountBefore = failures.size();
        Schema origSchema = this.schema;
        this.schema = schema;
        task.run();
        this.schema = origSchema;
        int failureCountAfter = failures.size(), newFailureCount = failureCountAfter - failureCountBefore;
        if (newFailureCount == 0) {
            return null;
        } else if (newFailureCount == 1) {
            return failures.remove(failures.size() - 1);
        } else {
            List<ValidationException> newFailures = new ArrayList<>(failures.subList(failureCountBefore, failures.size()));
            int toBeRemoved = newFailureCount, lastIndex = failureCountAfter;
            while (toBeRemoved-- > 0) {
                failures.remove(--lastIndex);
            }
            return ValidationException.createWrappingException(schema, newFailures);
        }
    }

    int failureCount() {
        return failures.size();
    }

}
