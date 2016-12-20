package org.everit.json.schema.loader;

import org.json.JSONPointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author erosb
 */
public class BaseJSONVisitor implements JSONVisitor {

    List<String> pointer = new ArrayList<>();

    public void visitBoolean(boolean value) {

    }

    public void visitArray(List<JSONTraverser> value) {
        for (int i = 0; i < value.size(); ++i) {
            pointer.add(String.valueOf(i));
            value.get(i).accept(this);
            removeLastFragment();
        }
    }

    public void visitString(String value) {

    }

    public void visitInteger(Integer value) {

    }

    public void visitObject(Map<String, JSONTraverser> obj) {
        for (Map.Entry<String, JSONTraverser> entry: obj.entrySet()) {
            pointer.add(entry.getKey());
            entry.getValue().accept(this);
            removeLastFragment();
        }
    }

    private void removeLastFragment() {
        pointer.remove(pointer.size() - 1);
    }

    protected String getCurrentPointer() {
        return new JSONPointer(pointer).toURIFragment();
    }
}
