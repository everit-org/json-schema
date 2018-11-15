package org.everit.json.schema.loader;

import java.io.InputStream;

public class SchemaClients {

    static <T extends SchemaClient> InputStream apply(T client, final String url) {
        return client.get(url);
    }
}
