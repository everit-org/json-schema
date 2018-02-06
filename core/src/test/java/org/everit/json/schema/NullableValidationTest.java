package org.everit.json.schema;

import org.json.JSONObject;
import org.junit.Test;

public class NullableValidationTest {

    @Test
    public void testNullableFalse_JSONNull() {
        TestSupport.failureOf(StringSchema.builder().requiresString(false).nullable(false))
                .input(JSONObject.NULL)
                .expectedKeyword("nullable")
                .expectedMessageFragment("value cannot be null")
                .expect();
    }

    @Test
    public void testNullableFalse_nullReference() {
        TestSupport.failureOf(StringSchema.builder().requiresString(false).nullable(false))
                .input(null)
                .expectedKeyword("nullable")
                .expectedMessageFragment("value cannot be null")
                .expect();
    }

}
