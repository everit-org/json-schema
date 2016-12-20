package org.everit.json.schema.loader;

import org.json.JSONPointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author erosb
 */
public interface JSONVisitor {

    void visitBoolean(boolean value);

    void visitArray(List<JSONTraverser> value);

    void visitString(String value);

    void visitInteger(Integer value);

    void visitObject(Map<String, JSONTraverser> obj);

}
