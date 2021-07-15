package org.everit.json.schema;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import org.everit.json.schema.event.CombinedSchemaMatchEvent;
import org.everit.json.schema.event.CombinedSchemaMismatchEvent;
import org.everit.json.schema.event.ValidationListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ValidatingVisitorTest {

    private ValidationFailureReporter reporter;

    @BeforeEach
    public void before() {
        reporter = mock(ValidationFailureReporter.class);
    }

    @Nested
    class PassesTypeCheckTests {

        @Test
        public void otherType_noRequires() {
            ValidatingVisitor subject = createValidatingVisitor();
            assertFalse(subject.passesTypeCheck(JSONObject.class, false, null));
            verifyZeroInteractions(reporter);
        }

        private ValidatingVisitor createValidatingVisitor() {
            return new ValidatingVisitor("string", reporter, null, null, PrimitiveParsingPolicy.STRICT);
        }

        @Test
        public void otherType_requires() {
            ValidatingVisitor subject = createValidatingVisitor();
            assertFalse(subject.passesTypeCheck(JSONObject.class, true, null));
            verify(reporter).failure(JSONObject.class, "string");
        }

        @Test
        public void otherType_nullPermitted_nullObject() {
            ValidatingVisitor subject = new ValidatingVisitor(JSONObject.NULL, reporter, null, null,
                    PrimitiveParsingPolicy.STRICT);
            assertFalse(subject.passesTypeCheck(JSONObject.class, true, Boolean.TRUE));
            verifyZeroInteractions(reporter);
        }

        @Test
        public void otherType_nullPermitted_nullReference() {
            ValidatingVisitor subject = new ValidatingVisitor(null, reporter, null, null, PrimitiveParsingPolicy.STRICT);
            assertFalse(subject.passesTypeCheck(JSONObject.class, true, Boolean.TRUE));
            verifyZeroInteractions(reporter);
        }

        @Test
        public void nullPermitted_nonNullValue() {
            ValidatingVisitor subject = createValidatingVisitor();
            assertFalse(subject.passesTypeCheck(JSONObject.class, true, Boolean.TRUE));
            verify(reporter).failure(JSONObject.class, "string");
        }

        @Test
        public void requiresType_nullableIsNull() {
            ValidatingVisitor subject = new ValidatingVisitor(null, reporter, null, null, PrimitiveParsingPolicy.STRICT);
            assertFalse(subject.passesTypeCheck(JSONObject.class, true, null));
            verify(reporter).failure(JSONObject.class, null);
        }

        @Test
        public void sameType() {
            ValidatingVisitor subject = createValidatingVisitor();
            assertTrue(subject.passesTypeCheck(String.class, true, Boolean.TRUE));
            verifyZeroInteractions(reporter);
        }

    }

    public static Arguments[] permittedTypes() {
        return new Arguments[] {
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

    public static Arguments[] notPermittedTypes() {
        return new Arguments[] {
                Arguments.of(new Object[] { new ArrayList<String>() }),
                Arguments.of(new Object[] { new RuntimeException() })
        };
    }

    @ParameterizedTest
    @MethodSource("permittedTypes")
    public void permittedTypeSuccess(Object subject) {
        new ValidatingVisitor(subject, reporter, ReadWriteValidator.NONE, null, PrimitiveParsingPolicy.STRICT);
    }

    @ParameterizedTest
    @MethodSource("notPermittedTypes")
    public void notPermittedTypeFailure(Object subject) {
        assertThrows(IllegalArgumentException.class, () -> {
            new ValidatingVisitor(subject, reporter, ReadWriteValidator.NONE, null, PrimitiveParsingPolicy.STRICT);
        });
    }

    @Test
    public void triggersCombinedSchemaEvents() {
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

        new ValidatingVisitor(instance, reporter, ReadWriteValidator.NONE, listener, PrimitiveParsingPolicy.STRICT)
                .visit(combinedSchema);

        ValidationException exc = new InternalValidationException(stringSchema, String.class, instance);
        verify(listener).combinedSchemaMismatch(new CombinedSchemaMismatchEvent(combinedSchema, stringSchema, instance, exc));
        verify(listener).combinedSchemaMatch(new CombinedSchemaMatchEvent(combinedSchema, emptySchema, instance));
        verify(listener).combinedSchemaMatch(new CombinedSchemaMatchEvent(combinedSchema, objectSchema, instance));
    }

}
