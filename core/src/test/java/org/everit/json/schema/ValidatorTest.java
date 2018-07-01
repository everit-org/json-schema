package org.everit.json.schema;

import static org.everit.json.schema.TestSupport.loadAsV7;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.JsonValue;
import org.junit.Test;

public class ValidatorTest {

    private static final ObjectSchema RW_SCHEMA = (ObjectSchema) loadAsV7(ResourceLoader.DEFAULT.readObj("read-write-context.json"));

    @Test
    public void testCollectAllMode() {
        Validator actual = Validator.builder().build();
        try {
            actual.performValidation(ObjectSchemaTest.MULTIPLE_VIOLATIONS_SCHEMA,
                    ResourceLoader.DEFAULT.readObj("objecttestcases.json").get("multipleViolations"));
            fail("did not throw exception");
        } catch (ValidationException e) {
            assertEquals("#: 3 schema violations found", e.getMessage());
            assertEquals(3, e.getCausingExceptions().size());
        }
    }

    @Test
    public void testFailEarlyMode() {
        Validator actual = Validator.builder().failEarly().build();
        try {
            actual.performValidation(ObjectSchemaTest.MULTIPLE_VIOLATIONS_SCHEMA,
                    ResourceLoader.DEFAULT.readObj("objecttestcases.json").get("multipleViolations"));
            fail("did not throw exception");
        } catch (ValidationException e) {
            assertEquals("#: required key [boolProp] not found", e.getMessage());
            assertTrue("no causing exceptions", e.getCausingExceptions().isEmpty());
        }
    }

    @Test
    public void readOnlyContext() {
        Validator subject = Validator.builder()
                .readWriteContext(ReadWriteContext.READ)
                .build();
        JsonObject input = (JsonObject)JsonValue.of(JsonSchemaUtil.stringToNode("{\"writeOnlyProp\":3}"));
        TestSupport.failureOf(RW_SCHEMA)
                .expectedPointer("#/writeOnlyProp")
                .expectedSchemaLocation("#/properties/writeOnlyProp")
                .expectedKeyword("writeOnly")
                .expectedViolatedSchema(RW_SCHEMA.getPropertySchemas().get("writeOnlyProp"))
                .input(input)
                .validator(subject)
                .expect();
    }

    @Test
    public void writeOnlyContext() {
        Validator subject = Validator.builder()
                .readWriteContext(ReadWriteContext.WRITE)
                .build();
        JsonObject input = (JsonObject)JsonValue.of(JsonSchemaUtil.stringToNode("{\"readOnlyProp\":\"foo\"}"));
        TestSupport.failureOf(RW_SCHEMA)
                .expectedPointer("#/readOnlyProp")
                .expectedSchemaLocation("#/properties/readOnlyProp")
                .expectedKeyword("readOnly")
                .expectedViolatedSchema(RW_SCHEMA.getPropertySchemas().get("readOnlyProp"))
                .input(input)
                .validator(subject)
                .expect();
    }

    @Test
    public void readOnlyNullValue() {
        Validator subject = Validator.builder()
                .failEarly()
                .readWriteContext(ReadWriteContext.READ)
                .build();
        JsonObject input = (JsonObject)JsonValue.of(JsonSchemaUtil.stringToNode("{\"writeOnlyProp\":null}"));
        TestSupport.failureOf(RW_SCHEMA)
                .expectedPointer("#/writeOnlyProp")
                .expectedSchemaLocation("#/properties/writeOnlyProp")
                .expectedKeyword("writeOnly")
                .expectedViolatedSchema(RW_SCHEMA.getPropertySchemas().get("writeOnlyProp"))
                .input(input)
                .validator(subject)
                .expect();
    }

}
