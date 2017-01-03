package org.everit.json.schema.loader;

import java.util.List;
import java.util.Map;

/**
 * @author erosb
 */
class BaseJSONVisitor<R> implements JSONVisitor<R> {

    @Override
    public R visitBoolean(Boolean value, LoadingState ls) {
        return null;
    }

    @Override
    public R visitArray(List<JsonValue> value, LoadingState ls) {
        value.forEach(val -> val.accept(this));
        return null;
    }

    @Override
    public R visitString(String value, LoadingState ls) {
        return null;
    }

    @Override
    public R visitInteger(Integer value, LoadingState ls) {
        return null;
    }

    @Override
    public R visitObject(Map<String, JsonValue> obj, LoadingState ls) {
        obj.values().forEach(val -> val.accept(this));
        return null;
    }

    @Override
    public R visitNull(LoadingState ls) {
        return null;
    }

    @Override public R finishedVisiting(LoadingState ls) {
        return null;
    }

}
