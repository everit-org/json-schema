package org.everit.json.schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.everit.json.schema.loader.JsonArray;
import org.everit.json.schema.loader.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class JsonSchemaUtil {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map objectNodeToMap(ObjectNode node) {
		HashMap map = new HashMap();
		Iterator<Entry<String, JsonNode>> fields = ((ObjectNode)node).fields();
		while(fields.hasNext()) {
    		Entry<String, JsonNode> entry = fields.next();
    		JsonNode value = entry.getValue();
    		String key = entry.getKey();
    		if(value instanceof ObjectNode) {
    			map.put(key, new JsonObject((Map)nodeToObject(value)));
    		} else if (value instanceof ArrayNode) {
    			map.put(key, new JsonArray((List)nodeToObject(value)));
    		} else if (value instanceof NullNode) {
    			map.put(key, JsonObject.NULL);
    		} else {
    			map.put(key, nodeToValue(value));
    		}
    	}
		
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List arrayNodeToList (ArrayNode node) {
		List list = new ArrayList();
		
		ArrayNode arr = (ArrayNode)node;
		for (int i=0; i<arr.size(); i++) {
			JsonNode jsonNode = arr.get(i);
			if (jsonNode instanceof ObjectNode) {
				list.add(new JsonObject((Map)nodeToObject(jsonNode)));
			} else if (jsonNode instanceof ArrayNode) {
				list.add(new JsonArray((List)nodeToObject(jsonNode)));
			} else if (jsonNode instanceof NullNode) {
				list.add(JsonObject.NULL);
			} else {
				list.add(nodeToValue(jsonNode));
			}
			
		}
		
		return list;
	}
	
	@SuppressWarnings("rawtypes")
	public static Object nodeToObject(JsonNode node) {
		if (node instanceof ObjectNode) {
			Map nodeToMap = objectNodeToMap((ObjectNode)node);
			return nodeToMap;
		} else if (node instanceof ArrayNode) {
			List list = arrayNodeToList((ArrayNode)node);
			return list;
		} else if (node instanceof NullNode) {
			return JsonObject.NULL;
		} else {
        	return nodeToValue(node);
        }
	}
	
	private static Object nodeToValue(JsonNode node) {
		ObjectMapper mapper = new ObjectMapper();
		Object value = mapper.convertValue(node, Object.class);
		if (value instanceof TextNode) {
			value = ((TextNode)value).asText();
		}
		
		if (value instanceof BooleanNode) {
			value = ((BooleanNode)value).asBoolean();
		}
		
		if (value instanceof NumericNode) {
			value = (NumericNode)value;
			if (((NumericNode) value).canConvertToInt()) {
				value = ((IntNode)value).asInt();
			}
			
			if (((NumericNode) value).isDouble()) {
				value = ((DoubleNode)value).asDouble();
			}
		}
		return value;
	}
	
	public static JsonNode stringToNode(String str) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = null;
	    try {
			node = mapper.readTree(str);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	    return node;
	}
	
	public static JsonNode streamToNode(InputStream stream) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = null;
	    try {
			node = mapper.readTree(stream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	    return node;
	}
}
