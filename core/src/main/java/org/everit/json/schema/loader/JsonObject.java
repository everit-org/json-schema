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

import org.everit.json.schema.JsonPointer;
import org.everit.json.schema.SchemaException;

/**
 * @author erosb
 */
public class JsonObject extends JsonValue {

	public static final Object NULL = new NullJsonObject();
	
	private final Map<String, Object> storage;
	
	public JsonObject() {
		this(new HashMap());
	}
	
    public JsonObject(Map<String, Object> storage) {
        super(storage);
        this.storage = storage;
    }

    public void put (String key, Object value) {
    	storage.put(key, value);
    }
    
    public void remove (String key) {
    	storage.remove(key);
    }
    
    public int size() {
    	return storage.size();
    }
    
    JsonValue childFor(String key) {
        return ls.childFor(key);
    }
    
    public String[] getNames() {
        if (storage == null || storage.size() == 0) {
            return null;
        }
        return storage.keySet().toArray(new String[storage.size()]);
    }
    
    public boolean containsKey(String key) {
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

    @Override public Object value() {
        return this;
    }

    @Override public Object unwrap() {
        return new HashMap<>(storage);
    }
    
    public Map<String, Object> toMap() {
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
    
    public Object query(String jsonPointer) {
        return query(new JsonPointer(jsonPointer));
    }
    
    public Object query(JsonPointer jsonPointer) {
        return jsonPointer.queryFrom(this);
    }
}
