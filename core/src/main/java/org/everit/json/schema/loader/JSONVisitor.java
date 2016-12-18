package org.everit.json.schema.loader;

import java.util.List;
import java.util.Map;

/**
 * @author erosb
 */
public class JSONVisitor {

    public void visitBoolean(boolean value) {

    }

    void visitArray(List<JSONTraverser> value) {
        value.forEach(val -> val.accept(this));
    }

    public void visitString(String value) {

    }

    public void visitInteger(Integer value) {
        
    }

    public void visitObject(Map<String, JSONTraverser> obj) {
        obj.values().stream().forEach(val -> val.accept(this));
    }
}
