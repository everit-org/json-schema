package org.everit.json.schema.javax.json;

import javax.json.Json;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * An adaptation for the JSON types of JSR-353 for support of Java EE 7.
 * <p>
 * In JSR-353, none of the {@link Json} methods to create JSON scalar
 * values are available. However there are methods to create array
 * and object builders. To create a single scalar value, in this
 * implementation we use an array builder; adding an intrinsic scalar
 * value and then extracting it from the completed array.
 * <p>
 * While this approach has some additional overhead, it at least provides
 * a means to support Java EE 7 which includes the JSR-353 specification
 * of the standard JSON types.
 */
public class Jsr353Adaptation extends JavaxJsonAdaptation {

    @Override
    JsonValue createValue(String value) {
        return Json.createArrayBuilder().add(value).build().get(0);
    }

    @Override
    JsonValue createValue(int value) {
        return Json.createArrayBuilder().add(value).build().get(0);
    }

    @Override
    JsonValue createValue(double value) {
        return Json.createArrayBuilder().add(value).build().get(0);
    }

    @Override
    JsonValue createValue(long value) {
        return Json.createArrayBuilder().add(value).build().get(0);
    }

    @Override
    JsonValue createValue(BigInteger value) {
        return Json.createArrayBuilder().add(value).build().get(0);
    }

    @Override
    JsonValue createValue(BigDecimal value) {
        return Json.createArrayBuilder().add(value).build().get(0);
    }

}
