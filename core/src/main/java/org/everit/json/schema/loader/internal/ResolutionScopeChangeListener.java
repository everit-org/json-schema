/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema.loader.internal;

import org.everit.json.schema.Consumer;

import java.net.URI;

/**
 * Event handler interface used by {@link TypeBasedMultiplexer} to notify client(s) (which is
 * currently a schema loader instance) about resolution scope changes.
 */
public abstract class ResolutionScopeChangeListener extends Consumer<URI> {

    @Override
    public void accept(final URI t) {
        resolutionScopeChanged(t);
    }

    public abstract void resolutionScopeChanged(URI newResolutionScope);
}
