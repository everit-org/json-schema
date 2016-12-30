package org.everit.json.schema.loader;

import org.json.JSONPointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author erosb
 */
public class BaseJSONVisitor<R> implements JSONVisitor<R> {

    @Override
    public R visitBoolean(boolean value, LoadingState ls) {
        return null;
    }

    @Override
    public R visitArray(List<JSONTraverser> value, LoadingState ls) {
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
    public R visitObject(Map<String, JSONTraverser> obj, LoadingState ls) {
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
