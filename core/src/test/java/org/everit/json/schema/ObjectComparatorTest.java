package org.everit.json.schema;


import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ObjectComparatorTest {

    public static final JSONArray EMPTY_ARRAY = new JSONArray();
    public static final JSONObject EMPTY_OBJECT = new JSONObject();

    private static List<Arguments> failingCases() {
        return Stream.of(
            Arguments.of("array, null", EMPTY_ARRAY, null),
            Arguments.of("array, object", EMPTY_ARRAY, EMPTY_OBJECT),
            Arguments.of("object, null", EMPTY_OBJECT, null),
            Arguments.of("arrays with different length", EMPTY_ARRAY, new JSONArray("[null]")),
            Arguments.of("arrays with different elems", new JSONArray("[true, false]"), new JSONArray("[false, true]")),
            Arguments.of("objects with different length", EMPTY_OBJECT, new JSONObject("{\"a\":true}")),
            Arguments.of("number and not number", EMPTY_OBJECT, 1)
        ).collect(Collectors.toList());
    }

    @ParameterizedTest
    @MethodSource("failingCases")
    public void array_Null_failure(String testcaseName, Object arg1, Object arg2) {
        assertFalse(ObjectComparator.deepEquals(arg1, arg2));
        assertFalse(ObjectComparator.deepEquals(arg2, arg1));
    }

}
