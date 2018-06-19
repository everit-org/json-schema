package org.everit.json.schema.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.everit.json.schema.Schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonPrinter {

	private static final JsonFactory factory = new JsonFactory();
    private final JsonGenerator jg;

    public JsonPrinter(final Writer writer) {
    	try {
			jg = factory.createGenerator(writer);
			//jg.setPrettyPrinter(new DefaultPrettyPrinter());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    public JsonPrinter key(final String key) {
    	try {
			jg.writeFieldName(key);
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return this;
    }

    public JsonPrinter value(final Object value) {
    	try {
    		ObjectMapper mapper = new ObjectMapper();
    		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    		String jsonString = mapper.writeValueAsString(value);
    		jg.writeRawValue(jsonString);
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return this;
    }

    public JsonPrinter object() {
    	try {
			jg.writeStartObject();
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return this;
    }

    public JsonPrinter endObject() {
    	try {
			jg.writeEndObject();
			jg.flush();
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return this;
    }

    public JsonPrinter ifPresent(final String key, final Object value) {
        if (value != null) {
            key(key);
            value(value);
        }
        return this;
    }

    public JsonPrinter ifTrue(final String key, final Boolean value) {
        if (value != null && value) {
            key(key);
            value(value);
        }
        return this;
    }

    public JsonPrinter array() {
    	try {
			jg.writeStartArray();
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return this;
    }

    public JsonPrinter endArray() {
    	try {
			jg.writeEndArray();
			jg.flush();
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return this;
    }

    public void ifFalse(String key, Boolean value) {
        if (value != null && !value) {
            try {
				jg.writeFieldName(key);
				jg.writeBoolean(value);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
        }
    }

    public <K> void printSchemaMap(Map<K, Schema> input) {
        object();
        input.entrySet().forEach(entry -> {
            key(entry.getKey().toString());
            entry.getValue().describeTo(this);
        });
        endObject();
    }
}
