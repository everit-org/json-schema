package org.everit.json.schema.loader.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;

import org.everit.json.schema.loader.SchemaClient;

/**
 * A {@link SchemaClient} implementation which uses {@link URL} for reading the remote content.
 */
public class DefaultSchemaClient implements SchemaClient {

    @Override
    public InputStream get(final String url) {
        try {
            return (InputStream) new URL(url).getContent();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
