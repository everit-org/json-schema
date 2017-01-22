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

import com.google.common.base.Throwables;
import org.everit.json.schema.loader.SchemaClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A {@link SchemaClient} implementation which uses {@link URL} for reading the remote content.
 */
public class DefaultSchemaClient extends SchemaClient {

    @Override
    public InputStream get(final String url) {
        try {
            return (InputStream) new URL(url).getContent();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
