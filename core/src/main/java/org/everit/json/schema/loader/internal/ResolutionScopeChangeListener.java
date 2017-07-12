package org.everit.json.schema.loader.internal;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Event handler interface used by {@link TypeBasedMultiplexer} to notify client(s) (which is
 * currently a schema loader instance) about resolution scope changes.
 */
@FunctionalInterface
public interface ResolutionScopeChangeListener extends Consumer<URI> {

    @Override
    default void accept(final URI t) {
        resolutionScopeChanged(t);
    }

    void resolutionScopeChanged(URI newResolutionScope);
}
