package org.everit.json.schema.loader;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author erosb
 */
public abstract class JSONVisitor {

    public abstract void visitBoolean(boolean value);

    void visitArray(List<JSONTraverser> value) {
        value.forEach(val -> val.accept(this));
    }

    public abstract void visitString(String value);

    public abstract void visitInteger(Integer value);

    public void visitObject(Map<String, JSONTraverser> obj) {
        obj.values().stream().forEach(val -> val.accept(this));
    }
}
