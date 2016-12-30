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
        for (int i = 0; i < value.size(); ++i) {
            value.get(i).accept(this);
        }
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
        for (Map.Entry<String, JSONTraverser> entry: obj.entrySet()) {
            entry.getValue().accept(this);
        }
        return null;
    }

}
