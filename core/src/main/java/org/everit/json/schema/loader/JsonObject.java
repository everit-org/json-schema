package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
final class JsonObject extends JsonValue {

    private final Map<String, Object> storage;

    JsonObject(Map<String, Object> storage) {
        super(storage, null);
        this.storage = storage;
        this.ls = new LoadingState(SchemaLoader.builder()
                .rootSchemaJson(this)
                .schemaJson(this));
    }

    JsonObject(Map<String, Object> storage, LoadingState ls) {
        super(storage, ls.childForId(requireNonNull(storage, "storage cannot be null").get("id")));
        this.storage = storage;
    }

    boolean containsKey(String key) {
        return storage.containsKey(key);
    }

    void require(String key, Consumer<JsonValue> consumer) {
        if (storage.containsKey(key)) {
            LoadingState childState = ls.childFor(key);
            consumer.accept(JsonValue.of(storage.get(key), childState));
        } else {
            throw failureOfMissingKey(key);
        }
    }

    JsonValue require(String key) {
        return requireMapping(key, e -> e);
    }

    <R> R requireMapping(String key, Function<JsonValue, R> fn) {
        if (storage.containsKey(key)) {
            LoadingState childState = ls.childFor(key);
            return fn.apply(JsonValue.of(storage.get(key), childState));
        } else {
            throw failureOfMissingKey(key);
        }
    }

    private SchemaException failureOfMissingKey(String key) {
        return ls.createSchemaException(format("required key [%s] not found", key));
    }

    void maybe(String key, Consumer<JsonValue> consumer) {
        if (storage.containsKey(key)) {
            LoadingState childState = ls.childFor(key);
            consumer.accept(JsonValue.of(storage.get(key), childState));
        }
    }

    Optional<JsonObject> maybe(String key) {
        return maybeMapping(key, identity());
    }

    <R> Optional<R> maybeMapping(String key, Function<JsonValue, R> fn) {
        if (storage.containsKey(key)) {
            LoadingState childState = ls.childFor(key);
            return Optional.of(fn.apply(JsonValue.of(storage.get(key), childState)));
        } else {
            return Optional.empty();
        }
    }

    void forEach(JsonObjectIterator iterator) {
        storage.entrySet().forEach(entry -> iterateOnEntry(entry, iterator));
    }

    private void iterateOnEntry(Map.Entry<String, Object> entry, JsonObjectIterator iterator) {
        String key = entry.getKey();
        LoadingState childState = ls.childFor(key);
        iterator.apply(key, JsonValue.of(entry.getValue(), childState));
    }

    @Override public <R> R requireObject(Function<JsonObject, R> mapper) {
        return mapper.apply(this);
    }

    @Override protected Class<?> typeOfValue() {
        return JsonObject.class;
    }

    @Override protected Object value() {
        return this;
    }

    Map<String, Object> toMap() {
        return unmodifiableMap(storage);
    }

    boolean isEmpty() {
        return storage.isEmpty();
    }

    public Set<String> keySet() {
        return unmodifiableSet(storage.keySet());
    }

    public Object get(String name) {
        return storage.get(name);
    }
}
