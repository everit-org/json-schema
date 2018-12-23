package org.everit.json.schema;

import static org.everit.json.schema.ConditionalSchemaTest.MAX_LENGTH_STRING_SCHEMA;
import static org.everit.json.schema.ConditionalSchemaTest.MIN_LENGTH_STRING_SCHEMA;
import static org.everit.json.schema.ConditionalSchemaTest.PATTERN_STRING_SCHEMA;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.ELSE;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.IF;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.THEN;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.everit.json.schema.event.ConditionalSchemaMatchEvent;
import org.everit.json.schema.event.ConditionalSchemaMismatchEvent;
import org.everit.json.schema.event.ValidationListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConditionalSchemaEventsTest {

    private ConditionalSchema schema = ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA)
            .thenSchema(MIN_LENGTH_STRING_SCHEMA)
            .elseSchema(MAX_LENGTH_STRING_SCHEMA).schemaLocation("#").build();

    @Mock
    ValidationListener listener;

    private ValidationFailureReporter reporter;

    @Before public void before() {
        reporter = new CollectingFailureReporter(schema);
    }

    private void validateInstance(String instance) {
        try {
            Validator.builder()
                    .withListener(listener)
                    .build()
                    .performValidation(schema, instance);
        } catch (ValidationException e) {
            // intentionally ignored
        }
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

    @Test
    public void ifMismatch_elseMatch() {
        String instance = "boo";
        validateInstance(instance);

        ValidationException failure = new ValidationException(PATTERN_STRING_SCHEMA, "string [boo] does not match pattern f.*o",
                "pattern", "#/if");
        verify(listener).ifSchemaMismatch(new ConditionalSchemaMismatchEvent(schema, instance, IF, failure));
        verify(listener).elseSchemaMatch(new ConditionalSchemaMatchEvent(schema, instance, ELSE));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void ifMismatch_elseMismatch() {
        String instance = "booooooooooooo";
        validateInstance(instance);

        ValidationException ifFailure = new ValidationException(PATTERN_STRING_SCHEMA,
                "string [booooooooooooo] does not match pattern f.*o",
                "pattern", "#/if");
        verify(listener).ifSchemaMismatch(new ConditionalSchemaMismatchEvent(schema, instance, IF, ifFailure));
        ValidationException elseFailure = new ValidationException(MAX_LENGTH_STRING_SCHEMA,
                "expected maxLength: 4, actual: 14",
                "maxLength", "#/else");
        verify(listener).elseSchemaMismatch(new ConditionalSchemaMismatchEvent(schema, instance, ELSE, elseFailure));
        verifyNoMoreInteractions(listener);
    }

}
