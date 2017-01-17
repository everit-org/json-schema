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
package org.everit.json.schema.loader;

import com.google.common.base.Function;

import java.io.InputStream;

/**
 * This interface is used by {@link SchemaLoader} to fetch the contents denoted by remote JSON
 * pointer.
 * <p>
 * <p>
 * Implementations are expected to support the HTTP/1.1 protocol, the support of other protocols is
 * optional.
 * </p>
 */
public abstract class SchemaClient implements Function<String, InputStream> {

    @Override
    public InputStream apply(final String url) {
        return get(url);
    }

    /**
     * Returns a stream to be used for reading the remote content (response body) of the URL. In the
     * case of a HTTP URL, implementations are expected send HTTP GET requests and the response is
     * expected to be represented in UTF-8 character set.
     *
     * @param url the URL of the remote resource
     * @return the input stream of the response
     * @throws java.lang.RuntimeException if an IO error occurs.
     */
    public abstract InputStream get(String url);

}
