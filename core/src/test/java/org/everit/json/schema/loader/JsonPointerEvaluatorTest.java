package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.everit.json.schema.TestSupport.asStream;
import static org.everit.json.schema.loader.JsonValueTest.withLs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.SchemaLocation;
import org.everit.json.schema.loader.internal.DefaultProviderValidators;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonPointerEvaluatorTest {

    private static final JsonObject rootSchemaJson = withLs(JsonValue.of(ResourceLoader.DEFAULT.readObj("testschemas.json")
            .getJSONObject("refPointerDerivatedFromPointer").toMap())).requireObject();

    private static final ResourceLoader LOADER = ResourceLoader.DEFAULT;

    @Test
    void sameDocumentSuccess() {
        JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#/definitions/Bar");
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertEquals("dummy schema at #/definitions/Bar", actual.require("description").requireString());
        assertEquals("http://localhost:1234/folder/", actual.ls.id.toString());
        assertEquals(new SchemaLocation(asList("definitions", "Bar")), actual.ls.pointerToCurrentObj);
    }

    @Test
    void sameDocumentNotFound() {
        Assertions.assertThrows(SchemaException.class, () -> {
            JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#/definitions/NotFound");
            JsonObject actual = pointer.query().getQueryResult().requireObject();
            assertEquals("dummy schema at #/definitions/Bar", actual.require("description").requireString());
            assertEquals("http://localhost:1234/folder/", actual.ls.id.toString());
            assertEquals(new SchemaLocation(asList("definitions", "Bar")), actual.ls.pointerToCurrentObj);
        });
    }

    @Test
    void arrayIndexSuccess() {
        JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#/definitions/Array/0");
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertEquals("dummy schema in array", actual.require("description").requireString());
    }

    @Test
    void rootRefSuccess() {
        JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#");
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertSame(rootSchemaJson, actual);
    }

    @Test
    void escaping() {
        JsonPointerEvaluator pointer = JsonPointerEvaluator.forDocument(rootSchemaJson, "#/definitions/Escaping/sla~1sh/ti~0lde");
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertEquals("tiled", actual.require("description").requireString());
    }

    private LoadingState createLoadingState(SchemaClient schemaClient, String ref) {
        LoaderConfig config = new LoaderConfig(schemaClient, new DefaultProviderValidators(), SpecificationVersion.DRAFT_4, false);
        URI parentScopeId = null;
        Object rootSchemaJson = this.rootSchemaJson;
        HashMap<String, Object> schemaJson = new HashMap<>();
        schemaJson.put("$ref", ref);
        return new LoadingState(config, new HashMap<>(), rootSchemaJson, schemaJson, parentScopeId, SchemaLocation.empty());
    }

    @Test
    void remoteDocumentSuccess() throws URISyntaxException {
        SchemaClient schemaClient = mock(SchemaClient.class);
        when(schemaClient.get("http://localhost:1234/hello")).thenReturn(rootSchemaJsonAsStream());
        JsonPointerEvaluator pointer = JsonPointerEvaluator
                .forURL(schemaClient, "http://localhost:1234/hello#/definitions/Bar",
                        createLoadingState(schemaClient, "#/definitions/Foo"));
        JsonObject actual = pointer.query().getQueryResult().requireObject();
        assertEquals("dummy schema at #/definitions/Bar", actual.require("description").requireString());
        assertEquals("http://localhost:1234/folder/", actual.ls.id.toString());
        assertEquals(new SchemaLocation(new URI("http://localhost:1234/hello"), asList("definitions", "Bar")),
                actual.ls.pointerToCurrentObj);
    }
    
    @Test
    void remoteDocument_jsonParsingFailure() {
        SchemaClient schemaClient = mock(SchemaClient.class);
        when(schemaClient.get("http://localhost:1234/hello")).thenReturn(asStream("unparseable"));
        SchemaException actual = assertThrows(SchemaException.class,
            () -> JsonPointerEvaluator.forURL(schemaClient, "http://localhost:1234/hello#/foo", createLoadingState(schemaClient, "")).query()
        );
        assertEquals("http://localhost:1234/hello", actual.getSchemaLocation());
    }

    @Test
    void schemaExceptionForInvalidURI() {
        try {
            SchemaClient schemaClient = mock(SchemaClient.class);
            JsonPointerEvaluator subject = JsonPointerEvaluator.forURL(schemaClient, "||||",
                    createLoadingState(schemaClient, "#/definitions/Foo"));
            subject.query();
            fail("did not throw exception");
        } catch (SchemaException e) {
            assertThat(e.toJSON(), sameJsonAs(LOADER.readObj("pointer-eval-non-uri-failure.json")));
        }
    }

    protected InputStream rootSchemaJsonAsStream() {
        return asStream(new JSONObject(rootSchemaJson.toMap()).toString());
    }
}

