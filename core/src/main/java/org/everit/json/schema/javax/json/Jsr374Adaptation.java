package org.everit.json.schema.javax.json;

import javax.json.Json;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * An adaptation for the JSON types of JSR-374 for support of Java EE 8.
 */
public class Jsr374Adaptation extends JavaxJsonAdaptation {

    @Override
    JsonValue createValue(String value) {
        return Json.createValue(value);
    }

    @Override
    JsonValue createValue(int value) {
        return Json.createValue(value);
    }

    @Override
    JsonValue createValue(double value) {
        return Json.createValue(value);
    }

    @Override
    JsonValue createValue(long value) {
        return Json.createValue(value);
    }

    @Override
    JsonValue createValue(BigInteger value) {
        return Json.createValue(value);
    }

    @Override
    JsonValue createValue(BigDecimal value) {
        return Json.createValue(value);
    }

}
