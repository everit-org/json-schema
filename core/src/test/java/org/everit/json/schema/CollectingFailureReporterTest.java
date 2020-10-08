package org.everit.json.schema;

import static java.util.Arrays.asList;
import static org.everit.json.schema.ValidationException.createWrappingException;
import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class CollectingFailureReporterTest {

    public static final Runnable NOOP = () -> {
    };

    @Test
    public void noNewExceptions_returnsNull() {
        ValidationException actual = createSubject().inContextOfSchema(NullSchema.INSTANCE, NOOP);
        assertNull(actual);
    }

    @Test
    public void subSchemaIsNull() {
        assertThrows(NullPointerException.class, () -> {
            createSubject().inContextOfSchema(null, () -> {
            });
        });
    }

    @Test
    public void singleExceptionAdded_andReturned() {
        CollectingFailureReporter subject = createSubject();
        ValidationException entry = new ValidationException(NullSchema.INSTANCE, JSONObject.NULL.getClass(), "string");

        ValidationException actual = subject.inContextOfSchema(NullSchema.INSTANCE, () -> {
            subject.failure(entry);
        });

        assertSame(entry, actual);
        assertEquals(0, subject.failureCount());
    }

    @Test
    public void multipleFailures_areWrapped() {
        CollectingFailureReporter subject = createSubject();
        ValidationException entry1 = new ValidationException(FalseSchema.builder().build(), JSONObject.NULL.getClass(), "string");
        ValidationException entry2 = new ValidationException(NullSchema.INSTANCE, JSONObject.NULL.getClass(), "string");

        ValidationException expected = createWrappingException(NullSchema.INSTANCE, asList(entry1, entry2));

        ValidationException actual = subject.inContextOfSchema(NullSchema.INSTANCE, () -> {
            subject.failure(entry1);
            subject.failure(entry2);
        });

        assertEquals(expected, actual);
        assertEquals(0, subject.failureCount());
    }

    private CollectingFailureReporter createSubject() {
        return new CollectingFailureReporter(BooleanSchema.INSTANCE);
    }
}
