package org.everit.json.schema.loader.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;

import org.everit.json.schema.loader.SchemaClient;

/**
 * A {@link SchemaClient} implementation which uses {@link URL} for reading the remote content.
 */
public class DefaultSchemaClient implements SchemaClient {

    @Override
    public InputStream get(final String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            String location = conn.getHeaderField("Location");
            if (location != null) {
                return get(location);
            }
            return (InputStream) conn.getContent();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
