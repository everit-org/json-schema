package org.everit.json.schema;

import org.junit.Test;

public class ConditionalSchemaTest {

    public static final StringSchema MAX_LENGTH_STRING_SCHEMA = StringSchema.builder().maxLength(4).build();
    public static final StringSchema MIN_LENGTH_STRING_SCHEMA = StringSchema.builder().minLength(4).build();
    public static final StringSchema PATTERN_STRING_SCHEMA = StringSchema.builder().pattern("f.*o").build();

    @Test
    public void onlyIfSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().ifSchema(StringSchema.builder().maxLength(2).build()).build().validate("foo");
    }

    @Test
    public void onlyIfSuccess() {
        ConditionalSchema.builder().ifSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    @Test
    public void onlyThenSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().thenSchema(StringSchema.builder().maxLength(2).build()).build().validate("foo");
    }

    @Test
    public void onlyThenSuccess() {
        ConditionalSchema.builder().thenSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    @Test
    public void onlyElseSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().elseSchema(StringSchema.builder().maxLength(1).build()).build().validate("foo");
    }

    @Test
    public void onlyElseSuccess() {
        ConditionalSchema.builder().elseSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaSuccessThenSubschemaFailure() {
        ConditionalSchema.builder().ifSchema(MAX_LENGTH_STRING_SCHEMA).thenSchema(PATTERN_STRING_SCHEMA).build().validate("bar");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaFailureThenSubschemaSuccess() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("bar");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaFailureThenSubschemaFailure() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foobar");
    }

    @Test
    public void ifSubschemaSuccessThenSubschemaSuccess() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

    @Test
    public void ifSubschemaSuccessThenSubschemaSuccessElseSubSchemaFailure() {
        ConditionalSchema.builder().ifSchema(PATTERN_STRING_SCHEMA).thenSchema(MAX_LENGTH_STRING_SCHEMA).elseSchema(MIN_LENGTH_STRING_SCHEMA).build().validate("foo");
    }

}