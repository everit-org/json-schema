package org.everit.json.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ValidatorTest {

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

}
