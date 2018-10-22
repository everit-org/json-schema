package org.everit.json.schema.facade.orgjson;

import org.everit.json.schema.facade.JsonArray;
import org.json.JSONArray;

import java.util.Collection;

final class Array extends JSONArray implements JsonArray {
    Array(Collection<Object> elements) {
        super(elements);
    }

    @Override
    public <T> T unsafe(Class<T> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        }
        return JsonArray.super.unsafe(type);
    }
}
