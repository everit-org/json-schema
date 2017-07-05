package org.everit.json.schema.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
final class JsonArray extends JsonValue {

    private List<Object> storage;

    JsonArray(List<Object> storage, LoadingState ls) {
        super(storage, ls);
        this.storage = requireNonNull(storage, "storage cannot be null");
    }

    public JsonArray(List<Object> storage) {
        super(storage);
        this.storage = requireNonNull(storage, "storage cannot be null");
    }

    public void forEach(JsonArrayIterator iterator) {
        for (int i = 0; i < storage.size(); ++i) {
            JsonValue childValue = at(i);
            iterator.apply(i, childValue);
        }
    }

    protected JsonValue at(int i) {
        Object raw = storage.get(i);
        LoadingState childState = ls.childFor(i);
        return JsonValue.of(raw, childState);
    }

    public int length() {
        return storage.size();
    }

    @Override public <R> R requireArray(Function<JsonArray, R> mapper) {
        return mapper.apply(this);
    }

    @Override
    protected Class<?> typeOfValue() {
        return JsonArray.class;
    }

    @Override protected Object value() {
        return this;
    }

    protected Object unwrap() {
        return new ArrayList<>(storage);
    }
}
