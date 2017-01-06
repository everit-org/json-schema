package org.everit.json.schema.loader;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
public class JsonArray extends JsonValue {

    private List<Object> storage;

    public JsonArray(List<Object> storage, LoadingState ls) {
        super(storage, ls);
        this.storage = requireNonNull(storage, "storage cannot be null");
    }
}
