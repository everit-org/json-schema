package org.everit.json.schema.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URI;
import java.net.URISyntaxException;

import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.ResourceLoader;
import org.junit.jupiter.api.Test;

public class RegisteredURIResolutionTest {

    private static final ResourceLoader LOADER = new ResourceLoader("/org/everit/jsonvalidator/registered-uris/");

    @Test
    public void success() throws URISyntaxException {
        SchemaClient mock = mock(SchemaClient.class);
        SchemaLoader loader = SchemaLoader.builder()
                .schemaClient(mock)
                .schemaJson(LOADER.readObj("ref-urn.json"))
                .registerSchemaByURI(new URI("urn:uuid:a773c7a2-1a13-4f6a-a70d-694befe0ce63"), LOADER.readObj("schema-by-urn.json"))
                .build();

        ReferenceSchema actual = (ReferenceSchema) loader.load().build();

        assertEquals("schema-by-urn", actual.getReferredSchema().getTitle());
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void urnWithFragment() throws URISyntaxException {
        SchemaClient mock = mock(SchemaClient.class);
        SchemaLoader loader = SchemaLoader.builder()
                .schemaClient(mock)
                .schemaJson(LOADER.readObj("ref-urn-fragment.json"))
                .registerSchemaByURI(new URI("urn:uuid:a773c7a2-1a13-4f6a-a70d-694befe0ce63"), LOADER.readObj("schema-by-urn.json"))
                .build();

        ReferenceSchema actual = (ReferenceSchema) loader.load().build();

        assertEquals("subschema-by-urn", actual.getReferredSchema().getTitle());
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void httpURL() throws URISyntaxException {
        SchemaClient mock = mock(SchemaClient.class);
        SchemaLoader loader = SchemaLoader.builder()
                .schemaClient(mock)
                .schemaJson(LOADER.readObj("ref-example-org.json"))
                .registerSchemaByURI(new URI("http://example.org"), LOADER.readObj("schema-by-urn.json"))
                .build();

        ReferenceSchema actual = (ReferenceSchema) loader.load().build();

        assertEquals("schema-by-urn", actual.getReferredSchema().getTitle());
        verifyNoMoreInteractions(mock);
    }

}
