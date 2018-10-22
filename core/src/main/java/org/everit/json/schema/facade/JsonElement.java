package org.everit.json.schema.facade;

public interface JsonElement {
    // Turns this Element into an arbitrary Object. Mainly intended for backwards compatibility.
    // May throw UnsupportedOperationException if Object type is not supported. E.g. Json Org expected but not provided
    // Type may not be null => NPE
    // Subclasses should call parent / interface
    default <T> T unsafe(Class<T> type) {
        throw new UnsupportedOperationException(
            String.format(
                "Representing '%s' as '%s' is not supported.\nPlease consult the documentation of your Facade",
                this.getClass().getCanonicalName(),
                type.getCanonicalName()
            )
        );
    }
}
