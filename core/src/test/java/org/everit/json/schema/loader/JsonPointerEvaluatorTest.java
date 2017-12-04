package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.everit.json.schema.TestSupport.asStream;
import static org.everit.json.schema.loader.JsonValueTest.withLs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.SchemaException;
import org.json.JSONObject;
import org.junit.Test;

public class JsonPointerEvaluatorTest {

    private static final JsonObject rootSchemaJson = withLs(JsonValue.of(ResourceLoader.DEFAULT.readObj("testschemas.json")
            .getJSONObject("refPointerDerivatedFromPointer").toMap())).requireObject();

    @Test
    public void sameDocumentSuccess() {
        JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#/definitions/Bar");
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertEquals("dummy schema at #/definitions/Bar", actual.require("description").requireString());
        assertEquals("http://localhost:1234/folder/", actual.ls.id.toString());
        assertEquals(asList("definitions", "Bar"), actual.ls.pointerToCurrentObj);
    }

    @Test(expected = SchemaException.class)
    public void sameDocumentNotFound() {
        JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#/definitions/NotFound");
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertEquals("dummy schema at #/definitions/Bar", actual.require("description").requireString());
        assertEquals("http://localhost:1234/folder/", actual.ls.id.toString());
        assertEquals(asList("definitions", "Bar"), actual.ls.pointerToCurrentObj);
    }

    @Test
    public void arrayIndexSuccess() {
        JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#/definitions/Array/0");
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertEquals("dummy schema in array", actual.require("description").requireString());
    }

    @Test
    public void rootRefSuccess() {
        JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#");
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertSame(rootSchemaJson, actual);
    }

    @Test
    public void escaping() {
        JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#/definitions/Escaping/sla~1sh/ti~0lde");
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertEquals("tiled", actual.require("description").requireString());
    }

    private LoadingState createLoadingState(SchemaClient httpClient, String ref) {
        LoaderConfig config = new LoaderConfig(httpClient, emptyMap(), SpecificationVersion.DRAFT_4, false);
        URI parentScopeId = null;
        Object rootSchemaJson = this.rootSchemaJson;
        HashMap<String, Object> schemaJson = new HashMap<>();
        schemaJson.put("$ref", ref);
        return new LoadingState(config, new HashMap<>(), rootSchemaJson, schemaJson, parentScopeId, new ArrayList<>());
    }

    @Test
    public void remoteDocumentSuccess() {
        SchemaClient httpClient = mock(SchemaClient.class);
        when(httpClient.get("http://localhost:1234/hello")).thenReturn(rootSchemaJsonAsStream());
        JsonPointerEvaluator pointer = JsonPointerEvaluator
                .forURL(httpClient, "http://localhost:1234/hello#/definitions/Bar", createLoadingState(httpClient, "#/definitions/Foo"));
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertEquals("dummy schema at #/definitions/Bar", actual.require("description").requireString());
        assertEquals("http://localhost:1234/folder/", actual.ls.id.toString());
        assertEquals(asList("definitions", "Bar"), actual.ls.pointerToCurrentObj);
    }

    protected InputStream rootSchemaJsonAsStream() {
        return asStream(new JSONObject(rootSchemaJson.toMap()).toString());
    }
}
