package org.everit.json.schema.loader;

import java.util.HashMap;
import java.util.Map;

class SubschemaRegistry {

    final Map<String, JsonObject> storage = new HashMap<>();


    SubschemaRegistry(JsonValue rootJson) {
        collectObjectsWithId(rootJson);
    }

    void collectObjectsWithId(JsonValue val) {
        String idKeyword = val.ls.specVersion().idKeyword();
        if (val instanceof JsonObject) {
            JsonObject obj = (JsonObject) val;
            if (obj.containsKey(idKeyword)
                && obj.require(idKeyword).typeOfValue() == String.class) {
                storage.put(obj.require(idKeyword).requireString(), obj);
            }
            for (String key : obj.keySet()) {
                collectObjectsWithId(obj.require(key));
            }
        } else if (val instanceof JsonArray) {
            JsonArray arr = (JsonArray) val;
            for (int i = 0; i < arr.length(); ++i) {
                collectObjectsWithId(arr.at(i));
            }
        }
    }

    JsonObject getById(String id) {
        return storage.get(id);
    }
}
