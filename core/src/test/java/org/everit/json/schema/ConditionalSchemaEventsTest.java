package org.everit.json.schema;

import static org.everit.json.schema.ConditionalSchemaTest.MAX_LENGTH_STRING_SCHEMA;
import static org.everit.json.schema.ConditionalSchemaTest.MIN_LENGTH_STRING_SCHEMA;
import static org.everit.json.schema.ConditionalSchemaTest.PATTERN_STRING_SCHEMA;
import static org.everit.json.schema.listener.ConditionalSchemaValidationEvent.Keyword.IF;
import static org.everit.json.schema.listener.ConditionalSchemaValidationEvent.Keyword.THEN;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.everit.json.schema.listener.ConditionalSchemaMatchEvent;
import org.everit.json.schema.listener.ConditionalSchemaMismatchEvent;
import org.everit.json.schema.listener.ValidationListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConditionalSchemaEventsTest {

    private ConditionalSchema schema = ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA)
            .thenSchema(MIN_LENGTH_STRING_SCHEMA)
            .elseSchema(MAX_LENGTH_STRING_SCHEMA).schemaLocation("#").build();

    @Mock
    ValidationListener listener;

    @Spy
    ValidationFailureReporter reporter = spy(new CollectingFailureReporter(schema));

    private void validateInstance(String instance) {
        new ValidatingVisitor(instance, reporter, ReadWriteValidator.NONE, listener).visit(schema);
    }

    @Test
    public void ifMatch_thenMatch() {
        String instance = "f###oo";
        validateInstance(instance);
        verify(listener).ifSchemaMatch(new ConditionalSchemaMatchEvent(schema, instance, IF));
        verify(listener).thenSchemaMatch(new ConditionalSchemaMatchEvent(schema, instance, THEN));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void ifMatch_thenMismatch() {
        String instance = "foo";
        validateInstance(instance);
        verify(listener).ifSchemaMatch(new ConditionalSchemaMatchEvent(schema, instance, IF));
        ValidationException failure = new ValidationException(MIN_LENGTH_STRING_SCHEMA,
                "expected minLength: 6, actual: 3", "minLength",
                "#/then");
        verify(listener).thenSchemaMismatch(new ConditionalSchemaMismatchEvent(schema, instance, THEN, failure));
        verifyNoMoreInteractions(listener);
    }

}
