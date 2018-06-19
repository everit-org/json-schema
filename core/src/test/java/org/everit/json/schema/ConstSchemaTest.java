package org.everit.json.schema;

import static org.everit.json.schema.TestSupport.loadAsV6;
import static org.junit.Assert.assertEquals;

import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.JsonValue;
import org.junit.Test;

public class ConstSchemaTest {

    private static final ResourceLoader LOADER = ResourceLoader.DEFAULT;

    private void testSuccess(Object value) {
        ConstSchema subject = ConstSchema.builder().permittedValue(value).build();
        subject.validate(value);
    }

    private void testSuccess(Object permitted, Object subjectOfValidation) {
        ConstSchema subject = ConstSchema.builder().permittedValue(permitted).build();
        subject.validate(subjectOfValidation);
    }

    private void testFailure(Object permitted, Object subjectOfValidation) {
        TestSupport.failureOf(ConstSchema.builder().permittedValue(permitted))
                .input(subjectOfValidation)
                .expectedKeyword("const")
                .expect();
    }

    @Test
    public void successWithInteger() {
        testSuccess(2);
    }

    @Test
    public void failureWithInteger() {
        testFailure(2, 3);
    }

    @Test
    public void successWithNull() {
        testSuccess(null, JsonObject.NULL);
        testSuccess(JsonObject.NULL, null);
    }

    @Test
    public void failureWithNull() {
        testFailure(JsonObject.NULL, "asd");
    }

    @Test
    public void successWithObject() {
    	JsonObject schemaJson = LOADER.readObj("constobject.json");
        loadAsV6(schemaJson).validate(schemaJson.get("const"));

        testSuccess(JsonValue.of(JsonSchemaUtil.stringToNode("{\"a\":\"b\", \"b\":\"a\"}")));
    }

    @Test
    public void toStringTest() {
        ConstSchema subject = ConstSchema.builder().permittedValue(true).build();
        String actual = subject.toString();
        assertEquals("{\"const\":true}", actual);
    }

    @Test
    public void toStringWithNull() {
        ConstSchema subject = ConstSchema.builder().permittedValue(null).build();
        String actual = subject.toString();
        assertEquals("{\"const\":null}", actual);
    }

    @Test
    public void toStringWithObject() {
        ConstSchema subject = ConstSchema.builder().permittedValue(JsonValue.of(JsonSchemaUtil.stringToNode("{\"a\":2}"))).build();
        String actual = subject.toString();
        assertEquals("{\"const\":{\"a\":2}}", actual);
    }

    @Test
    public void failureWithObject() {
        testFailure(JsonValue.of(JsonSchemaUtil.stringToNode("{}")), JsonValue.of(JsonSchemaUtil.stringToNode("{\"a\":null}")));
    }

    @Test
    public void successWithArray() {
        testSuccess(JsonValue.of(JsonSchemaUtil.stringToNode("[1,2,3]")));
    }

    @Test
    public void failureWithArray() {
        testFailure(JsonValue.of(JsonSchemaUtil.stringToNode("[1,2,3]")), JsonValue.of(JsonSchemaUtil.stringToNode("[3, 2,1]")));
    }

}
