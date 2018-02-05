package org.everit.json.schema;

import org.junit.Test;

public class ConditionalSchemaTest {

    private static final StringSchema MAX_LENGTH_STRING_SCHEMA = StringSchema.builder().maxLength(4).build();
    private static final StringSchema MIN_LENGTH_STRING_SCHEMA = StringSchema.builder().minLength(6).build();
    private static final StringSchema PATTERN_STRING_SCHEMA = StringSchema.builder().pattern("f.*o").build();

    // only if

    @Test
    public void onlyIfSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().ifSchema(StringSchema.builder().maxLength(2).build()).build().validate("foo");
    }

    @Test
    public void onlyIfSuccess() {
        ConditionalSchema.builder().ifSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    // only then

    @Test
    public void onlyThenSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().thenSchema(StringSchema.builder().maxLength(2).build()).build().validate("foo");
    }

    @Test
    public void onlyThenSuccess() {
        ConditionalSchema.builder().thenSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    // only else

    @Test
    public void onlyElseSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().elseSchema(StringSchema.builder().maxLength(1).build()).build().validate("foo");
    }

    @Test
    public void onlyElseSuccess() {
        ConditionalSchema.builder().elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    // if-then

    @Test(expected = ValidationException.class)
    public void ifSubschemaSuccessThenSubschemaFailure() {
        ConditionalSchema.builder().ifSchema(MAX_LENGTH_STRING_SCHEMA).thenSchema(PATTERN_STRING_SCHEMA).build().validate("bar");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaFailureThenSubschemaFailure() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("barbar");
    }

    @Test
    public void ifSubschemaSuccessThenSubschemaSuccess() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaFailureThenSubschemaSuccess() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("bar");
    }

    // if-else

    @Test
    public void ifSubschemaSuccessElseSubschemaFailure() {
        ConditionalSchema.builder().ifSchema(MAX_LENGTH_STRING_SCHEMA).elseSchema(PATTERN_STRING_SCHEMA).build().validate("bar");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaFailureElseSubschemaFailure() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("barbar");
    }

    @Test
    public void ifSubschemaSuccessElseSubschemaSuccess() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    @Test
    public void ifSubschemaFailureElseSubschemaSuccess() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("bar");
    }

    // then-else

    @Test
    public void thenSubschemaSuccessElseSubschemaFailure() {
        ConditionalSchema.builder().thenSchema(MAX_LENGTH_STRING_SCHEMA).elseSchema(PATTERN_STRING_SCHEMA).build().validate("bar");
    }

    @Test
    public void thenSubschemaFailureElseSubschemaFailure() {
        ConditionalSchema.builder().thenSchema(PATTERN_STRING_SCHEMA).elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("barbar");
    }

    @Test
    public void thenSubschemaSuccessElseSubschemaSuccess() {
        ConditionalSchema.builder().thenSchema(PATTERN_STRING_SCHEMA).elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    @Test
    public void thenSubschemaFailureElseSubschemaSuccess() {
        ConditionalSchema.builder().thenSchema(PATTERN_STRING_SCHEMA).elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("bar");
    }

    // if-then-else

    @Test
    public void ifSubschemaSuccessThenSubschemaSuccessElseSubSchemaSuccess() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    @Test
    public void ifSubschemaSuccessThenSubschemaSuccessElseSubSchemaFailure() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).elseSchema(MIN_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaSuccessThenSubschemaFailureElseSubSchemaSuccess() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).elseSchema(MIN_LENGTH_STRING_SCHEMA).build().validate("foobar");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaSuccessThenSubschemaFailureElseSubSchemaFailure() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).elseSchema(MIN_LENGTH_STRING_SCHEMA).build().validate("foooo");
    }

    @Test
    public void ifSubschemaFailureThenSubschemaSuccessElseSubSchemaSuccess() {
        ConditionalSchema.builder().ifSchema(MAX_LENGTH_STRING_SCHEMA).thenSchema(PATTERN_STRING_SCHEMA).elseSchema(MIN_LENGTH_STRING_SCHEMA).build().validate("foobar");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaFailureThenSubschemaSuccessElseSubSchemaFailure() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).elseSchema(MIN_LENGTH_STRING_SCHEMA).build().validate("bar");
    }

    @Test
    public void ifSubschemaFailureThenSubschemaFailureElseSubSchemaSuccess() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MIN_LENGTH_STRING_SCHEMA).elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("bar");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaFailureThenSubschemaFailureElseSubSchemaFailure() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MIN_LENGTH_STRING_SCHEMA).elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("barbarbar");
    }

}