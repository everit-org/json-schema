package org.everit.json.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.json.JSONArray;

class ArraySchemaValidatingVisitor extends Visitor {

    private Object subject;

    private JSONArray arraySubject;

    private int subjectLength;

    private ValidatingVisitor.FailureCollector failureCollector;

    public ArraySchemaValidatingVisitor(Object subject, ValidatingVisitor.FailureCollector failureCollector) {
        this.subject = subject;
        this.failureCollector = failureCollector;
    }

    @Override void visitArraySchema(ArraySchema arraySchema) {
        if (!(subject instanceof JSONArray)) {
            if (arraySchema.requiresArray()) {
                failureCollector.failure(JSONArray.class, subject);
            }
        } else {
            this.arraySubject = (JSONArray) subject;
            this.subjectLength = arraySubject.length();
            super.visitArraySchema(arraySchema);
        }
    }

    @Override void visitMinItems(Integer minItems) {
        if (minItems != null && subjectLength < minItems) {
            failureCollector.failure("expected minimum item count: " + minItems + ", found: " + subjectLength, "minItems");
        }
    }

    @Override void visitMaxItems(Integer maxItems) {
        if (maxItems != null && maxItems < subjectLength) {
            failureCollector.failure("expected maximum item count: " + maxItems + ", found: " + subjectLength, "maxItems");
        }
    }

    @Override void visitUniqueItems(boolean uniqueItems) {
        if (!uniqueItems || subjectLength == 0) {
            return;
        }
        Collection<Object> uniques = new ArrayList<Object>(subjectLength);
        for (int i = 0; i < subjectLength; ++i) {
            Object item = arraySubject.get(i);
            for (Object contained : uniques) {
                if (ObjectComparator.deepEquals(contained, item)) {
                    failureCollector.failure("array items are not unique", "uniqueItems");
                    return;
                }
            }
            uniques.add(item);
        }
    }

    @Override void visitAllItemSchema(Schema allItemSchema) {
        if (allItemSchema != null) {
            validateItemsAgainstSchema(IntStream.range(0, subjectLength),
                    arraySubject,
                    allItemSchema);
        }
    }

    @Override void visitItemSchema(int index, Schema itemSchema) {
        Object subject = arraySubject.get(index);
        String idx = String.valueOf(index);
        ifFails(itemSchema, subject)
                .map(exc -> exc.prepend(idx))
                .ifPresent(failureCollector::failure);
    }

    private void validateItemsAgainstSchema(final IntStream indices, final JSONArray items,
            final Schema schema) {
        validateItemsAgainstSchema(indices, items, i -> schema);
    }

    private void validateItemsAgainstSchema(final IntStream indices, final JSONArray items,
            final IntFunction<Schema> schemaForIndex) {
        for (int i : indices.toArray()) {
            String copyOfI = String.valueOf(i); // i is not effectively final so we copy it
            ifFails(schemaForIndex.apply(i), items.get(i))
                    .map(exc -> exc.prepend(copyOfI))
                    .ifPresent(failureCollector::failure);
        }
    }

    private Optional<ValidationException> ifFails(final Schema schema, final Object input) {
        try {
            schema.validate(input);
            return Optional.empty();
        } catch (ValidationException e) {
            return Optional.of(e);
        }
    }
}
