package org.everit.json.schema;

import org.junit.Test;

public class ConditionalSchemaTest {

    @Test
    public void onlyIfSuccessEvenIfDataIsInvalidAgainstSubschema() {
        ConditionalSchema.builder().ifSchema(StringSchema.builder().maxLength(2).build()).build().validate("foo");
    }

    @Test
    public void onlyIfSuccess() {
        ConditionalSchema.builder().ifSchema(StringSchema.builder().maxLength(4).build()).build().validate("foo");
    }
}