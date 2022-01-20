package org.everit.json.schema;

import static org.everit.json.schema.PrimitiveValidationStrategy.LENIENT;
import static org.everit.json.schema.PrimitiveValidationStrategy.STRICT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.everit.json.schema.event.CombinedSchemaMatchEvent;
import org.everit.json.schema.event.CombinedSchemaMismatchEvent;
import org.everit.json.schema.event.ValidationListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValidatingVisitorTest {

    private ValidationFailureReporter reporter;

    @BeforeEach
    void before() {
        reporter = mock(ValidationFailureReporter.class);
    }

    @Nested
    class PassesTypeCheckTests {

        Consumer<?> onPass;

        @BeforeEach
        void before() {
            onPass = mock(Consumer.class);
        }

        @AfterEach
        void after() {
            verifyNoMoreInteractions(reporter);
            verifyNoMoreInteractions(onPass);
        }

        private <E> Consumer<E> onPassConsumer() {
            return (Consumer<E>) onPass;
        }

        private void verifyTypeCheckDidNotPass() {
            verify(onPass, never()).accept(any());
        }

        private ValidatingVisitor createValidatingVisitor() {
            return createValidatingVisitor("string", STRICT);
        }

        private ValidatingVisitor createValidatingVisitor(Object instance,
                                                          PrimitiveValidationStrategy primitiveValidationStrategy) {
            return new ValidatingVisitor(instance, reporter,
                    ReadWriteValidator.NONE,
                    ValidationListener.NOOP,
                    primitiveValidationStrategy);
        }

        @Test
        void otherType_noRequires() {
            ValidatingVisitor subject = createValidatingVisitor();
            subject.ifPassesTypeCheck(JSONObject.class, false, null, onPassConsumer());
            verifyTypeCheckDidNotPass();
        }

        @Test
        void otherType_requires() {
            ValidatingVisitor subject = createValidatingVisitor();
            subject.ifPassesTypeCheck(JSONObject.class, true, null, onPassConsumer());
            verifyTypeCheckDidNotPass();
            verify(reporter).failure(JSONObject.class, "string");
        }

        @Test
        void otherType_nullPermitted_nullObject() {
            ValidatingVisitor subject = createValidatingVisitor(JSONObject.NULL, STRICT);
            subject.ifPassesTypeCheck(JSONObject.class, true, Boolean.TRUE, onPassConsumer());
            verifyTypeCheckDidNotPass();
        }

        @Test
        void otherType_nullPermitted_nullReference() {
            ValidatingVisitor subject = createValidatingVisitor(null, STRICT);
            subject.ifPassesTypeCheck(JSONObject.class, true, Boolean.TRUE, onPassConsumer());
            verifyTypeCheckDidNotPass();
        }

        @Test
        void nullPermitted_nonNullValue() {
            ValidatingVisitor subject = createValidatingVisitor();
            subject.ifPassesTypeCheck(JSONObject.class, true, Boolean.TRUE, onPassConsumer());
            verifyTypeCheckDidNotPass();
            verify(reporter).failure(JSONObject.class, "string");
        }

        @Test
        void requiresType_nullableIsNull() {
            ValidatingVisitor subject = createValidatingVisitor(null, STRICT);
            subject.ifPassesTypeCheck(JSONObject.class, true, null, onPassConsumer());
            verifyTypeCheckDidNotPass();
            verify(reporter).failure(JSONObject.class, null);
        }

        @Test
        void lenientMode_expectedString_actualString() {
            ValidatingVisitor subject = createValidatingVisitor("str", LENIENT);
            subject.ifPassesTypeCheck(String.class, true, Boolean.TRUE, onPassConsumer());
            verify(onPassConsumer()).accept("str");
        }

        @Test
        void lenientMode_expectedString_actualNumber() {
            ValidatingVisitor subject = createValidatingVisitor(2, LENIENT);
            subject.ifPassesTypeCheck(String.class, true, Boolean.TRUE, onPassConsumer());
            verify(onPassConsumer()).accept("2");
        }

        @Test
        void lenientMode_expectedBoolean_actualString() {
            ValidatingVisitor subject = createValidatingVisitor("Yes", LENIENT);
            subject.ifPassesTypeCheck(Boolean.class, true, Boolean.TRUE, onPassConsumer());
            verify(onPassConsumer()).accept(true);
        }

        @Test
        void lenientMode_expectedInteger_actualString() {
            ValidatingVisitor subject = createValidatingVisitor("2", LENIENT);
            subject.ifPassesTypeCheck(Integer.class, true, Boolean.TRUE, onPassConsumer());
            verify(onPassConsumer()).accept(2);
        }

        @Test
        void lenientMode_expecedInteger_actualBooleanAsString() {
            ValidatingVisitor subject = createValidatingVisitor("true", LENIENT);
            subject.ifPassesTypeCheck(Integer.class, true, Boolean.TRUE, onPassConsumer());
            verifyTypeCheckDidNotPass();
            verify(reporter).failure(Integer.class, "true");
        }

        @Test
        public void sameType() {
            ValidatingVisitor subject = createValidatingVisitor();
            subject.ifPassesTypeCheck(String.class, true, Boolean.TRUE, onPassConsumer());
            verify(onPassConsumer()).accept("string");
        }
    }

    public static Arguments[] permittedTypes() {
        return new Arguments[]{
                Arguments.of("str"),
                Arguments.of(1),
                Arguments.of(1L),
                Arguments.of(1.0),
                Arguments.of(1.0f),
                Arguments.of(new BigInteger("42")),
                Arguments.of(new BigDecimal("42.3")),
                Arguments.of(true),
                Arguments.of(new Object[]{null}),
                Arguments.of(JSONObject.NULL),
                Arguments.of(new JSONObject("{}")),
                Arguments.of(new JSONArray("[]"))
        };
    }

    static Arguments[] notPermittedTypes() {
        return new Arguments[]{
                Arguments.of(new ArrayList<String>()),
                Arguments.of(new RuntimeException())
        };
    }

    @ParameterizedTest
    @MethodSource("permittedTypes")
    void permittedTypeSuccess(Object subject) {
        new ValidatingVisitor(subject, reporter, ReadWriteValidator.NONE, null, STRICT);
    }

    @ParameterizedTest
    @MethodSource("notPermittedTypes")
    void notPermittedTypeFailure(Object subject) {
        assertThrows(IllegalArgumentException.class, () -> {
            new ValidatingVisitor(subject, reporter, ReadWriteValidator.NONE, null, STRICT);
        });
    }

    @Test
    void triggersCombinedSchemaEvents() {
        ValidationListener listener = mock(ValidationListener.class);
        StringSchema stringSchema = StringSchema.builder().requiresString(true).build();
        EmptySchema emptySchema = EmptySchema.builder().build();
        ObjectSchema objectSchema = ObjectSchema.builder().requiresObject(true).build();
        CombinedSchema combinedSchema = CombinedSchema.builder().criterion(CombinedSchema.ONE_CRITERION)
                .subschema(stringSchema)
                .subschema(emptySchema)
                .subschema(objectSchema)
                .build();
        ValidationFailureReporter reporter = new CollectingFailureReporter(combinedSchema);
        JSONObject instance = new JSONObject();

        new ValidatingVisitor(instance, reporter, ReadWriteValidator.NONE, listener, STRICT)
                .visit(combinedSchema);

        ValidationException exc = new InternalValidationException(stringSchema, String.class, instance);
        verify(listener).combinedSchemaMismatch(new CombinedSchemaMismatchEvent(combinedSchema, stringSchema, instance, exc));
        verify(listener).combinedSchemaMatch(new CombinedSchemaMatchEvent(combinedSchema, emptySchema, instance));
        verify(listener).combinedSchemaMatch(new CombinedSchemaMatchEvent(combinedSchema, objectSchema, instance));
    }

}
