package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

class ClassPathAwareSchemaClient implements SchemaClient {

    private static final List<String> HANDLED_PREFIXES = unmodifiableList(asList("classpath://", "classpath:/", "classpath:"));

    private final SchemaClient fallbackClient;

    ClassPathAwareSchemaClient(SchemaClient fallbackClient) {
        this.fallbackClient = requireNonNull(fallbackClient, "fallbackClient cannot be null");
    }

    @Override public InputStream get(String url) {
        return handleProtocol(url)
                .map(this::loadFromClasspath)
                .orElseGet(() -> fallbackClient.get(url));
    }

    private InputStream loadFromClasspath(String str) {
        return getClass().getResourceAsStream(str);
    }

    private Optional<String> handleProtocol(String url) {
        return HANDLED_PREFIXES.stream().filter(url::startsWith)
                .map(prefix -> "/" + url.substring(prefix.length()))
                .findFirst();
    }

}
