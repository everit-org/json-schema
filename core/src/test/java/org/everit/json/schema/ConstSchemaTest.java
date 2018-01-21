package org.everit.json.schema;

import static org.everit.json.schema.TestSupport.loadAsV6;
import static org.junit.Assert.assertEquals;

import org.json.JSONArray;
import org.json.JSONObject;
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
        testSuccess(null, JSONObject.NULL);
        testSuccess(JSONObject.NULL, null);
    }

    @Test
    public void failureWithNull() {
        testFailure(JSONObject.NULL, "asd");
    }

    @Test
    public void successWithObject() {
        JSONObject schemaJson = LOADER.readObj("constobject.json");
        loadAsV6(schemaJson).validate(schemaJson.get("const"));

        testSuccess(new JSONObject("{\"a\":\"b\", \"b\":\"a\"}"));
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
        ConstSchema subject = ConstSchema.builder().permittedValue(new JSONObject("{\"a\":2}")).build();
        String actual = subject.toString();
        assertEquals("{\"const\":{\"a\":2}}", actual);
    }

    @Test
    public void failureWithObject() {
        testFailure(new JSONObject("{}"), new JSONObject("{\"a\":null}"));
    }

    @Test
    public void successWithArray() {
        testSuccess(new JSONArray("[1,2,3]"));
    }

    @Test
    public void failureWithArray() {
        testFailure(new JSONArray("[1,2,3]"), new JSONArray("[3, 2,1]"));
    }

}
