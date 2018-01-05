package org.everit.json.schema;

import java.util.ArrayList;
import java.util.List;

class CollectingFailureReporter extends ValidationFailureReporter {

    private List<ValidationException> failures = new ArrayList<>(1);

    CollectingFailureReporter(Schema schema) {
        super(schema);
    }

    @Override
    public void failure(ValidationException exc) {
        failures.add(exc);
    }

    public void validationFinished() {
        ValidationException.throwFor(schema, failures);
    }

    public ValidationException inContextOfSchema(Schema schema, Runnable task) {
        int failureCountBefore = failures.size();
        super.inContextOfSchema(schema, task);
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
