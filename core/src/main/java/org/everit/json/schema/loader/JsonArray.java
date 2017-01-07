package org.everit.json.schema.loader;

import java.util.List;
import java.util.function.BiFunction;

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

    public void forEach(JsonArrayIterator iterator) {
        int i = 0;
        for (Object raw: storage) {
            LoadingState childState = ls.childFor(i);
            iterator.apply(i, JsonValue.of(raw, childState), childState);
            ++i;
        }
    }

    public int length() {
        return storage.size();
    }

    @Override public <R> R requireArray(BiFunction<JsonArray, LoadingState, R> mapper) {
        return mapper.apply(this, ls);
    }

    @Override
    protected Class<?> typeOfValue() {
        return JsonArray.class;
    }
}
