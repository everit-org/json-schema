package org.everit.json.schema.loader;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.everit.json.schema.SchemaException;

/**
 * @author erosb
 */
class JsonObject extends JsonValue {

    final Map<String, Object> storage;

    JsonObject(Map<String, Object> storage) {
        super(storage);
        this.storage = storage;
    }

    JsonValue childFor(String key) {
        return ls.childFor(key);
    }

    boolean containsKey(String key) {
        return storage.containsKey(key);
    }

    void require(String key, Consumer<JsonValue> consumer) {
        if (storage.containsKey(key)) {
            consumer.accept(childFor(key));
        } else {
            throw failureOfMissingKey(key);
        }
    }

    JsonValue require(String key) {
        return requireMapping(key, e -> e);
    }

    <R> R requireMapping(String key, Function<JsonValue, R> fn) {
        if (storage.containsKey(key)) {
            return fn.apply(childFor(key));
        } else {
            throw failureOfMissingKey(key);
        }
    }

    private SchemaException failureOfMissingKey(String key) {
        return ls.createSchemaException(format("required key [%s] not found", key));
    }

    void maybe(String key, Consumer<JsonValue> consumer) {
        if (storage.containsKey(key)) {
            consumer.accept(childFor(key));
        }
    }

    Optional<JsonValue> maybe(String key) {
        return maybeMapping(key, identity());
    }

    <R> Optional<R> maybeMapping(String key, Function<JsonValue, R> fn) {
        if (storage.containsKey(key)) {
            return Optional.of(fn.apply(childFor(key)));
        } else {
            return Optional.empty();
        }
    }

    void forEach(JsonObjectIterator iterator) {
        storage.entrySet().forEach(entry -> iterateOnEntry(entry, iterator));
    }

    private void iterateOnEntry(Map.Entry<String, Object> entry, JsonObjectIterator iterator) {
        String key = entry.getKey();
        iterator.apply(key, childFor(key));
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

    @Override protected Object unwrap() {
        return new HashMap<>(storage);
    }

    Map<String, Object> toMap() {
        if (storage == null) {
            return null;
        }
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
