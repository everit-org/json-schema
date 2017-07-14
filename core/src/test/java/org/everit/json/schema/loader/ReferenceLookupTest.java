package org.everit.json.schema.loader;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ReferenceLookupTest {

    private static Map<String, Object> rootSchemaJson;

    {
        rootSchemaJson = ResourceLoader.DEFAULT.readObj("testschemas.json").getJSONObject("refPointerDerivatedFromPointer").toMap();
    }

    private SchemaClient httpClient;

    @Before
    public void before() {
        httpClient = mock(SchemaClient.class);
    }

    /**
     * Creates a LoadingState instance for which the schemaJson is an object with a single key, a $ref containing the ref param
     *
     * @param ref
     * @return
     */
    private LoadingState createLoadingState(String ref) {
        LoaderConfig config = new LoaderConfig(httpClient, emptyMap(), SpecificationVersion.DRAFT_4);
        URI parentScopeId = null;
        Object rootSchemaJson = this.rootSchemaJson;
        HashMap<String, Object> schemaJson = new HashMap<>();
        schemaJson.put("$ref", ref);
        return new LoadingState(config, new HashMap<>(), rootSchemaJson, schemaJson, parentScopeId, new ArrayList<>());
    }

    @Test @Ignore
    public void sameDocumentLookup() {
        JsonValue jsonValue = createLoadingState("#/definitions").schemaJson;
        ReferenceLookup subject = new ReferenceLookup(jsonValue.ls);
        Schema.Builder<?> actual = subject.lookup("#/definitions", jsonValue.requireObject());
    }

}
