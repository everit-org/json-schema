package org.everit.json.schema.facade.orgjson;

import org.everit.json.schema.facade.JsonObject;
import org.json.JSONObject;

final class Obj extends JSONObject implements JsonObject {
    @Override
    public void set(String key, Object value) {
        put(key, value);
    }

    @Override
    public <T> T unsafe(Class<T> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        }
        return JsonObject.super.unsafe(type);
    }
}
