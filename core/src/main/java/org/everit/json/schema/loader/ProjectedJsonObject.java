package org.everit.json.schema.loader;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

class ProjectedJsonObject extends JsonObject {

    private final JsonObject original;

    private final Set<String> hiddenKeys;

    ProjectedJsonObject(JsonObject original, Set<String> hiddenKeys) {
        super(emptyMap());
        this.ls = original.ls;
        this.original = requireNonNull(original, "original cannot be null");
        this.hiddenKeys = requireNonNull(hiddenKeys, "hiddenKeys cannot be null");
    }

    @Override JsonValue childFor(String key) {
        return original.childFor(key);
    }

    @Override boolean containsKey(String key) {
        return isVisibleKey(key) && original.containsKey(key);
    }

    private boolean isVisibleKey(String key) {
        return !hiddenKeys.contains(key);
    }

    @Override void require(String key, Consumer<JsonValue> consumer) {
        throwExceptionIfNotVisible(key);
        original.require(key, consumer);
    }

    private void throwExceptionIfNotVisible(String key) {
        if (!isVisibleKey(key)) {
            throw ls.createSchemaException(format("required key [%s] not found", key));
        }
    }

    @Override JsonValue require(String key) {
        throwExceptionIfNotVisible(key);
        return original.require(key);
    }

    @Override <R> R requireMapping(String key, Function<JsonValue, R> fn) {
        throwExceptionIfNotVisible(key);
        return original.requireMapping(key, fn);
    }

    @Override void maybe(String key, Consumer<JsonValue> consumer) {
        if (isVisibleKey(key)) {
            original.maybe(key, consumer);
        }
    }

    @Override Optional<JsonValue> maybe(String key) {
        if (isVisibleKey(key)) {
            return original.maybe(key);
        } else {
            return Optional.empty();
        }
    }

    @Override <R> Optional<R> maybeMapping(String key, Function<JsonValue, R> fn) {
        if (isVisibleKey(key)) {
            return original.maybeMapping(key, fn);
        } else {
            return Optional.empty();
        }
    }

    @Override void forEach(JsonObjectIterator iterator) {
        original.forEach((key, value) -> {
            if (isVisibleKey(key)) {
                iterator.apply(key, value);
            }
        });
    }

    @Override protected Object unwrap() {
        Map<String, Object> storage = new HashMap<>(original.storage);
        removeHiddenKeysFrom(storage);
        return storage;
    }

    @Override Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(original.toMap());
        removeHiddenKeysFrom(map);
        return map;
    }

    private void removeHiddenKeysFrom(Map<String, Object> map) {
        hiddenKeys.forEach(map::remove);
    }

    @Override boolean isEmpty() {
        Set<String> origKeys = original.keySet();
        return original.isEmpty() || (origKeys.containsAll(hiddenKeys) && origKeys.size() == hiddenKeys.size());
    }

    @Override public Set<String> keySet() {
        Set<String> keys = new HashSet<>(original.keySet());
        keys.removeAll(hiddenKeys);
        return keys;
    }

    @Override public Object get(String name) {
        return super.get(name);
    }
}
