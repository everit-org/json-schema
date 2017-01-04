package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class JsonObject extends JsonValue {

    private final Map<String, Object> storage;

    private final LoadingState ls;

    JsonObject(Map<String, Object> storage, LoadingState ls) {
        super(storage, ls);
        this.storage = requireNonNull(storage, "storage cannot be null");
        this.ls = requireNonNull(ls, "ls cannot be null");
    }

    boolean containsKey(String key) {
        return storage.containsKey(key);
    }

    public void require(String key, BiConsumer<JsonValue, LoadingState> consumer) {
        if (storage.containsKey(key)) {
            LoadingState childState = ls.childFor(key);
            consumer.accept(JsonValue.of(storage.get(key), childState), childState);
        } else {
            throw failureOfMissingKey(key);
        }
    }

    public <R> R require(String key, BiFunction<JsonValue, LoadingState, R> fn) {
        if (storage.containsKey(key)) {
            LoadingState childState = ls.childFor(key);
            return fn.apply(JsonValue.of(storage.get(key), childState), childState);
        } else {
            throw failureOfMissingKey(key);
        }
    }

    private SchemaException failureOfMissingKey(String key) {
        return ls.createSchemaException(format("required key [%s] not found", key));
    }

    public void maybe(String key, BiConsumer<JsonValue, LoadingState> consumer) {
        if (storage.containsKey(key)) {
            LoadingState childState = ls.childFor(key);
            consumer.accept(JsonValue.of(storage.get(key), childState), childState);
        }
    }

    public <R> Optional<R> maybe(String key, BiFunction<JsonValue, LoadingState, R> fn) {
        if (storage.containsKey(key)) {
            LoadingState childState = ls.childFor(key);
            return Optional.of(fn.apply(JsonValue.of(storage.get(key), childState), childState));
        } else {
            return Optional.empty();
        }
    }
}
