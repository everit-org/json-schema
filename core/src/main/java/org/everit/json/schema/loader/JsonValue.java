package org.everit.json.schema.loader;

import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.everit.json.schema.SchemaException;
import org.everit.json.schema.JsonSchemaUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author erosb
 */
public class JsonValue {

    class Multiplexer<R> {

        protected Map<Class<?>, Function<?, R>> actions = new HashMap<>();

        Multiplexer(Class<?> expectedType, Function<?, R> mapper) {
            actions.put(expectedType, mapper);
        }

        <T> Multiplexer<R> orMappedTo(Class<T> expectedType, Function<T, R> mapper) {
            actions.put(expectedType, mapper);
            return this;
        }

        R requireAny() {
            if (typeOfValue() == null) {
                throw multiplexFailure();
            }
            Function<Object, R> consumer = (Function<Object, R>) actions.keySet().stream()
                    .filter(clazz -> clazz.isAssignableFrom(typeOfValue()))
                    .findFirst()
                    .map(actions::get)
                    .orElseThrow(() -> multiplexFailure());
            return consumer.apply(value());
        }

        protected SchemaException multiplexFailure() {
            return ls.createSchemaException(typeOfValue(), actions.keySet());
        }

    }

    class VoidMultiplexer extends Multiplexer<Void> {

        VoidMultiplexer(Class<?> expectedType, Consumer<?> consumer) {
            super(expectedType, obj -> {
                ((Consumer<Object>) consumer).accept(obj);
                return null;
            });
        }

        <T> VoidMultiplexer or(Class<T> expectedType, Consumer<T> consumer) {
            actions.put(expectedType, obj -> {
                ((Consumer<Object>) consumer).accept(obj);
                return null;
            });
            return this;
        }

    }

    private class VoidMultiplexerWithSchemaPredicate extends VoidMultiplexer {

        private Consumer<JsonValue> action;

        VoidMultiplexerWithSchemaPredicate(Consumer<JsonValue> action) {
            super(JsonObject.class, action);
            this.action = action;
        }

        @Override Void requireAny() {
            if (typeOfValue() == Boolean.class) {
                action.accept(JsonValue.this);
                return null;
            }
            return super.requireAny();
        }

        @Override
        protected SchemaException multiplexFailure() {
            Set<Class<?>> expectedTypes = new HashSet<>(actions.keySet());
            expectedTypes.add(Boolean.class);
            return ls.createSchemaException(typeOfValue(), expectedTypes);
        }
    }

    private static final Function<?, ?> IDENTITY = e -> e;

    static final <T, R> Function<T, R> identity() {
        return (Function<T, R>) IDENTITY;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static JsonValue of(Object obj) {
        if (obj instanceof JsonValue) {
            return (JsonValue) obj;
        } else if (obj instanceof ArrayNode) {
           	List list = JsonSchemaUtil.arrayNodeToList((ArrayNode)obj);
            return new JsonArray(list);
        } else if (obj instanceof ObjectNode) {
        	Map map = JsonSchemaUtil.objectNodeToMap((ObjectNode)obj);
            return new JsonObject(map);
        } else if (obj instanceof JsonNode) {
        	if(obj instanceof NullNode) {
        		//Special handling for NullNode
        		return (JsonObject)JsonObject.NULL;
        	} else {
        		//For ValueNode types, for e.g. BooleanNode, NumericNode, TextNode
	        	Object value = JsonSchemaUtil.nodeToObject((JsonNode)obj);
	        	return new JsonValue(value);
        	}
        } else {
        	return new JsonValue(obj);
        }
    }

    public Object value() {
        return obj;
    }

    public Object unwrap() {
    	return value();
    }
    
    protected final Object obj;

    protected LoadingState ls;

    public JsonValue(Object obj) {
        this.obj = obj;
    }

    public <T> VoidMultiplexer canBe(Class<T> expectedType, Consumer<T> consumer) {
        return new VoidMultiplexer(expectedType, consumer);
    }

    public VoidMultiplexer canBeSchema(Consumer<JsonValue> consumer) {
        if (DRAFT_4.equals(this.ls.specVersion())) {
            return new VoidMultiplexer(JsonObject.class, consumer);
        } else {
            return new VoidMultiplexerWithSchemaPredicate(consumer);
        }
    }

    public <T, R> Multiplexer<R> canBeMappedTo(Class<T> expectedType, Function<T, R> mapper) {
        return new Multiplexer<R>(expectedType, mapper);
    }

    protected Class<?> typeOfValue() {
    	return obj == null ? null : obj.getClass();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        JsonValue that = (JsonValue) o;

        return obj != null ? obj.equals(that.obj) : that.obj == null;

    }

    @Override public int hashCode() {
        return obj != null ? obj.hashCode() : 0;
    }

    @Override public String toString() {
        return "JsonValue{" +
                "obj=" + obj +
                '}';
    }

    public String requireString() {
        return requireString(identity());
    }

    public <R> R requireString(Function<String, R> mapper) {
    	if (obj instanceof String) {
            return mapper.apply((String) obj);
        }
        throw ls.createSchemaException(typeOfValue(), String.class);
    }

    public Boolean requireBoolean() {
        return requireBoolean(identity());
    }

    public <R> R requireBoolean(Function<Boolean, R> mapper) {
    	if (obj instanceof Boolean) {
            return mapper.apply((Boolean) obj);
        }
        throw ls.createSchemaException(typeOfValue(), Boolean.class);
    }

    public JsonObject requireObject() {
        return requireObject(identity());
    }

    public <R> R requireObject(Function<JsonObject, R> mapper) {
        throw ls.createSchemaException(typeOfValue(), JsonObject.class);
    }

    public JsonArray requireArray() {
        return requireArray(identity());
    }

    public <R> R requireArray(Function<JsonArray, R> mapper) {
        throw ls.createSchemaException(typeOfValue(), JsonArray.class);
    }

    public Number requireNumber() {
        return requireNumber(identity());
    }

    public <R> R requireNumber(Function<Number, R> mapper) {
    	if (obj instanceof Number) {
            return mapper.apply((Number) obj);
        }
        throw ls.createSchemaException(typeOfValue(), Number.class);
    }

    public Integer requireInteger() {
        return requireInteger(identity());
    }

    public <R> R requireInteger(Function<Integer, R> mapper) {
    	if (obj instanceof Integer) {
            return mapper.apply((Integer) obj);
        }
        throw ls.createSchemaException(typeOfValue(), Integer.class);
    }

    public static Object deepToOrgJson(JsonValue v) {
    	if (v instanceof JsonObject && ((JsonObject)v).equals(JsonObject.NULL)) {
            return JsonObject.NULL;
        } if (v instanceof JsonObject) {
            JsonObject obj = new JsonObject(new HashMap<String, Object>());
            ((JsonObject)v).forEach((key, value) -> obj.put(key, deepToOrgJson(value)));
            return obj;
        } else if (v instanceof JsonArray) {
            JsonArray array = new JsonArray(new ArrayList<Object>());
            ((JsonArray)v).forEach((index, value) -> array.add(deepToOrgJson(value)));
            return array;
        } else
            return v.unwrap();
    }
}
