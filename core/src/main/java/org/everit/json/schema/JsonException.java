package org.everit.json.schema;

@SuppressWarnings("serial")
public class JsonException extends RuntimeException {
    /**
     * Constructs a JsonException with an explanatory message.
     *
     * @param message
     * Detail about the reason for the exception.
     */
    public JsonException(final String message) {
        super(message);
    }

    /**
     * Constructs a JsonException with an explanatory message and cause.
     * 
     * @param message
     * Detail about the reason for the exception.
     * @param cause
     * The cause.
     */
    public JsonException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new JsonException with the specified cause.
     * 
     * @param cause
     * The cause.
     */
    public JsonException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
