package org.everit.json.schema.facade.orgjson;

import org.everit.json.schema.facade.Facade;
import org.everit.json.schema.facade.JsonArray;
import org.everit.json.schema.facade.JsonComparator;
import org.everit.json.schema.facade.JsonObject;
import org.everit.json.schema.facade.JsonWriter;
import org.json.JSONObject;

import java.io.Writer;
import java.util.Collection;

public final class JsonOrg implements Facade.FacadeImpl {
    private static final JsonComparator COMPARATOR = new Comparator();

    @Override
    public Object NULL() {
        return JSONObject.NULL;
    }

    @Override
    public JsonObject object() {
        return new Obj();
    }

    @Override
    public JsonArray array(Collection<Object> elements) {
        return new Array(elements);
    }

    @Override
    public JsonWriter writer(Writer writer) {
        return null;
    }

    @Override
    public JsonComparator comparator() {
        return COMPARATOR;
    }

}
