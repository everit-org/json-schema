package org.everit.json.schema.loader;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author erosb
 */
public final class JsonArray extends JsonValue {

	private List<Object> storage;

	public JsonArray(List<Object> storage) {
        super(storage);
        this.storage = requireNonNull(storage, "storage cannot be null");
    }

	public void add(Object obj) {
		storage.add(obj);
	}

	public Object get(int index) {
		return storage.get(index);
	}

	@SuppressWarnings("rawtypes")
	public List toList() {
		return Collections.unmodifiableList(storage);
	}
	
	public void forEach(JsonArrayIterator iterator) {
        for (int i = 0; i < storage.size(); ++i) {
            JsonValue childValue = at(i);
            iterator.apply(i, childValue);
        }
    }

    public JsonValue at(int i) {
        return ls.childFor(i);
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

    @Override public Object value() {
        return this;
    }

    @Override
    public Object unwrap() {
        return new ArrayList<>(storage);
    }    
}
