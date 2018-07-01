package org.everit.json.schema;

@SuppressWarnings("serial")
public class JsonPointerException extends JsonException {
     public JsonPointerException(String message) {
        super(message);
    }

    public JsonPointerException(String message, Throwable cause) {
        super(message, cause);
    }

}
