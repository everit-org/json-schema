package org.everit.json.schema.loader;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.everit.json.schema.TestSupport.asStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.junit.Before;
import org.junit.Test;

public class ReferenceLookupTest {

    private static final Map<String, Object> rootSchemaJson = ResourceLoader.DEFAULT.readObj("ref-lookup-tests.json").toMap();

    private static final String v4Subschema = ResourceLoader.DEFAULT.readObj("v4-referred-subschema.json").toString();

    private SchemaClient httpClient;

    @Before
    public void before() {
        httpClient = mock(SchemaClient.class);
    }

    private Schema performLookup(String pointerToRef) {
        JsonObject jsonValue = query(pointerToRef).requireObject();
        ReferenceLookup subject = new ReferenceLookup(jsonValue.ls);
        String refPointer = jsonValue.require("$ref").requireString();
        Schema.Builder<?> actual = subject.lookup(refPointer, jsonValue);
        ReferenceSchema ref = (ReferenceSchema) actual.build();
        return ref.getReferredSchema();
    }

    @Test
    public void sameDocumentLookup() {
        Schema actual = performLookup("#/properties/sameDocPointer");
        assertEquals("dummy schema at #/definitions/Bar", actual.getDescription());
    }

    private JsonValue query(String pointer) {
        LoadingState rootLs = new LoadingState(new LoaderConfig(httpClient, emptyMap(), SpecificationVersion.DRAFT_6, false),
                new HashMap<>(),
                rootSchemaJson,
                rootSchemaJson,
                null,
                emptyList()
        );
        return JsonPointerEvaluator.forDocument(rootLs.rootSchemaJson(), pointer).query().getQueryResult();
    }

    @Test
    public void sameDocumentLookupById() {
        Schema actual = performLookup("#/properties/lookupByDocLocalIdent");
        assertEquals("it has document-local identifier", actual.getDescription());
    }

    @Test
    public void absoluteRef() {
        when(httpClient.get("http://localhost/schema.json")).thenReturn(asStream("{\"description\":\"ok\"}"));
        Schema actual = performLookup("#/properties/absoluteRef");
        assertEquals("ok", actual.getDescription());
    }

    @Test
    public void withParentScope() {
        when(httpClient.get("http://localhost/child-ref")).thenReturn(asStream("{\"description\":\"ok\"}"));
        Schema actual = performLookup("#/properties/parent/child");
        assertEquals("ok", actual.getDescription());
    }

    @Test
    public void schemaVersionChange() {
        when(httpClient.get("http://localhost/child-ref")).thenReturn(asStream(v4Subschema));
        NumberSchema actual = (NumberSchema) performLookup("#/properties/definitionInRemote");
        assertTrue(actual.isExclusiveMinimum());
    }

}
