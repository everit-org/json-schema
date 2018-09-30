package org.everit.json.schema;

import org.everit.json.schema.spi.JsonAdaptation;
import org.everit.json.schema.spi.JsonAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.stream.Stream;

/**
 * A {@link JsonAdaptation} that uses {@link JSONArray} as the array type and
 * {@link JSONObject} as the object type.
 *
 */
class JSONAdaptation implements JsonAdaptation<Object> {

    private static final Class<?>[] SUPPORTED_TYPES = {
        JSONArray.class,
        JSONObject.class,
        JSONObject.NULL.getClass()
    };

    @Override
    public Class<?> arrayType() {
        return JSONArray.class;
    }

    @Override
    public Class<?> objectType() {
        return JSONObject.class;
    }

    @Override
    public Class<?>[] supportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public boolean isSupportedType(Class<?> type) {
        return Stream.of(SUPPORTED_TYPES).anyMatch(t -> t.isAssignableFrom(type));
    }

    @Override
    public boolean isNull(Object value) {
        return value == null || JSONObject.NULL.equals(value);
    }

    @Override
    public Object adapt(Object value) {
        if (JSONObject.NULL.equals(value)) {
            return null;
        } else if (value instanceof JSONArray) {
            return new JSONArrayAdapter((JSONArray) value);
        } else if (value instanceof JSONObject) {
            return new JSONObjectAdapter((JSONObject) value);
        } else {
            return value;
        }
    }

    @Override
    public Object invert(Object value) {
        if (value == null) {
            return JSONObject.NULL;
        } else if (value instanceof JsonAdapter) {
            return ((JsonAdapter) value).unwrap();
        } else {
            return value;
        }
    }

}
