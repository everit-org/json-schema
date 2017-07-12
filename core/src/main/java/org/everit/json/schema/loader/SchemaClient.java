package org.everit.json.schema.loader;

import java.io.InputStream;
import java.util.function.Function;

/**
 * This interface is used by {@link SchemaLoader} to fetch the contents denoted by remote JSON
 * pointer.
 * <p>
 * Implementations are expected to support the HTTP/1.1 protocol, the support of other protocols is
 * optional.
 */
@FunctionalInterface
public interface SchemaClient extends Function<String, InputStream> {

    @Override
    default InputStream apply(final String url) {
        return get(url);
    }

    /**
     * Returns a stream to be used for reading the remote content (response body) of the URL. In the
     * case of a HTTP URL, implementations are expected send HTTP GET requests and the response is
     * expected to be represented in UTF-8 character set.
     *
     * @param url
     *         the URL of the remote resource
     * @return the input stream of the response
     * @throws java.io.UncheckedIOException
     *         if an IO error occurs.
     */
    InputStream get(String url);

}
