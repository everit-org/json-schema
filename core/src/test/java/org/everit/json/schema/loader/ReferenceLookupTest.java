package org.everit.json.schema.loader;

import static java.util.Collections.emptyMap;
import static org.everit.json.schema.TestSupport.asStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaLocation;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReferenceLookupTest {

    private static final Map<String, Object> rootSchemaJson = ResourceLoader.DEFAULT.readObj("ref-lookup-tests.json").toMap();

    private static final String v4Subschema = ResourceLoader.DEFAULT.readObj("v4-referred-subschema.json").toString();

    private SchemaClient schemaClient;

    @BeforeEach
    public void before() {
        schemaClient = mock(SchemaClient.class);
    }

    private Schema performLookup(String pointerToRef) {
        ReferenceSchema ref = obtainReferenceSchema(pointerToRef);
        return ref.getReferredSchema();
    }

    private ReferenceSchema obtainReferenceSchema(String pointerToRef) {
        JsonObject jsonValue = query(pointerToRef).requireObject();
        ReferenceLookup subject = new ReferenceLookup(jsonValue.ls);
        String refPointer = jsonValue.require("$ref").requireString();
        Schema.Builder<?, ?> actual = subject.lookup(refPointer, jsonValue);
        return (ReferenceSchema) actual.build();
    }

    @Test
    public void referenceSchemaLocationIsSet() {
        when(schemaClient.get("http://localhost/child-ref")).thenReturn(asStream(v4Subschema));
        ReferenceSchema ref = obtainReferenceSchema("#/properties/definitionInRemote");
        assertEquals("http://localhost/child-ref#/definitions/SubSchema", ref.getSchemaLocation());
    }

    @Test
    public void sameDocumentLookup() {
        Schema actual = performLookup("#/properties/sameDocPointer");
        assertEquals("dummy schema at #/definitions/Bar", actual.getDescription());
    }

    private JsonValue query(String pointer) {
        LoadingState rootLs = new LoadingState(new LoaderConfig(schemaClient, emptyMap(), SpecificationVersion.DRAFT_6, false),
                new HashMap<>(),
                rootSchemaJson,
                rootSchemaJson,
                null,
                SchemaLocation.empty()
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
        when(schemaClient.get("http://localhost/schema.json")).thenReturn(asStream("{\"description\":\"ok\"}"));
        Schema actual = performLookup("#/properties/absoluteRef");
        assertEquals("ok", actual.getDescription());
    }

    @Test
    public void withParentScope() {
        when(schemaClient.get("http://localhost/child-ref")).thenReturn(asStream("{\"description\":\"ok\"}"));
        Schema actual = performLookup("#/properties/parent/child");
        assertEquals("ok", actual.getDescription());
    }

    @Test
    public void schemaVersionChange() {
        when(schemaClient.get("http://localhost/child-ref")).thenReturn(asStream(v4Subschema));
        NumberSchema actual = (NumberSchema) performLookup("#/properties/definitionInRemote");
        assertTrue(actual.isExclusiveMinimum());
    }

    @Test
    public void idAsJsonPointerWorks() {
        Schema actual = performLookup("#/properties/pointerToIdIsJsonPointer");
        assertEquals("the ID can be a JSON pointer", actual.getDescription());
    }

    @Test
    public void multipleReferencesToSameSchema() {
        JSONObject rawSchema = ResourceLoader.DEFAULT.readObj("multi-pointer.json");
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(rawSchema);

        ReferenceSchema aRefSchema = (ReferenceSchema) actual.getPropertySchemas().get("a");
        assertEquals(aRefSchema.getUnprocessedProperties(), ImmutableMap.of("unproc0", "unproc0 of A"));
        assertEquals("A side", aRefSchema.getTitle());
        assertEquals("length of the A side", aRefSchema.getDescription());
        assertEquals(SchemaLocation.parseURI("#/properties/a"), aRefSchema.getLocation());
        assertNotNull(aRefSchema.getReferredSchema());

        ReferenceSchema bRefSchema = (ReferenceSchema) actual.getPropertySchemas().get("b");
        assertEquals(bRefSchema.getUnprocessedProperties(), ImmutableMap.of("unproc0", "unproc0 of B"));
        assertEquals("length of the B side", bRefSchema.getDescription());
        assertEquals(SchemaLocation.parseURI("#/properties/b"), bRefSchema.getLocation());
        assertEquals("B side", bRefSchema.getTitle());
        assertNotNull(bRefSchema.getReferredSchema());

        assertSame(aRefSchema.getReferredSchema(), bRefSchema.getReferredSchema());

    }

}
