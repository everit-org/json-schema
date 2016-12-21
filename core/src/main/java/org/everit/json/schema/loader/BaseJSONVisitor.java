package org.everit.json.schema.loader;

import org.json.JSONPointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author erosb
 */
public class BaseJSONVisitor<R> implements JSONVisitor<R> {

    List<String> pointer = new ArrayList<>();

    public R visitBoolean(boolean value) {
        return null;
    }

    public R visitArray(List<JSONTraverser> value) {
        for (int i = 0; i < value.size(); ++i) {
            pointer.add(String.valueOf(i));
            value.get(i).accept(this);
            removeLastFragment();
        }
        return null;
    }

    public R visitString(String value) {
        return null;
    }

    public R visitInteger(Integer value) {
        return null;
    }

    public R visitObject(Map<String, JSONTraverser> obj) {
        for (Map.Entry<String, JSONTraverser> entry: obj.entrySet()) {
            pointer.add(entry.getKey());
            entry.getValue().accept(this);
            removeLastFragment();
        }
        return null;
    }

    private void removeLastFragment() {
        pointer.remove(pointer.size() - 1);
    }

    protected String getCurrentPointer() {
        return new JSONPointer(pointer).toURIFragment();
    }
}
