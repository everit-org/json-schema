package org.everit.json.schema;

import org.junit.Test;

public class ConditionalSchemaTest {

    public static final StringSchema STRING_SCHEMA_1 = StringSchema.builder().maxLength(4).build();
    public static final StringSchema STRING_SCHEMA_2 = StringSchema.builder().pattern("f.*o").build();

    @Test
    public void onlyIfSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().ifSchema(StringSchema.builder().maxLength(2).build()).build().validate("foo");
    }

    @Test
    public void onlyIfSuccess() {
        ConditionalSchema.builder().ifSchema(STRING_SCHEMA_1).build().validate("foo");
    }

    @Test
    public void onlyThenSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().thenSchema(StringSchema.builder().maxLength(2).build()).build().validate("foo");
    }

    @Test
    public void onlyThenSuccess() {
        ConditionalSchema.builder().thenSchema(STRING_SCHEMA_1).build().validate("foo");
    }

    @Test
    public void onlyElseSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().elseSchema(StringSchema.builder().maxLength(1).build()).build().validate("foo");
    }

    @Test
    public void onlyElseSuccess() {
        ConditionalSchema.builder().elseSchema(STRING_SCHEMA_1).build().validate("foo");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaSuccessThenSubschemaFailure() {
        ConditionalSchema.builder().ifSchema(STRING_SCHEMA_1).thenSchema(STRING_SCHEMA_2).build().validate("bar");
    }

    @Test(expected = ValidationException.class)
    public void ifSubschemaFailureThenSubschemaSuccess() {
        ConditionalSchema.builder().ifSchema(STRING_SCHEMA_2).thenSchema(STRING_SCHEMA_1).build().validate("bar");
    }
}