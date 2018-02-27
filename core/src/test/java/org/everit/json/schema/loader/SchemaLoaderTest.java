package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static org.everit.json.schema.TestSupport.asStream;
import static org.everit.json.schema.TestSupport.loadAsV6;
import static org.everit.json.schema.TestSupport.loadAsV7;
import static org.everit.json.schema.TestSupport.v6Loader;
import org.everit.json.schema.internal.URIV4FormatValidator;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ConditionalSchema;
import org.everit.json.schema.ConstSchema;
import org.everit.json.schema.EmptySchema;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.FalseSchema;
import org.everit.json.schema.NotSchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.TestSupport;
import org.everit.json.schema.TrueSchema;
import org.everit.json.schema.internal.DateTimeFormatValidator;
import org.everit.json.schema.internal.EmailFormatValidator;
import org.everit.json.schema.internal.HostnameFormatValidator;
import org.everit.json.schema.internal.IPV4Validator;
import org.everit.json.schema.internal.IPV6Validator;
import org.everit.json.schema.internal.URIFormatValidator;
import org.everit.json.schema.loader.SchemaLoader.SchemaLoaderBuilder;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class SchemaLoaderTest {

    private static JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("testschemas.json");

    private static JSONObject get(final String schemaName) {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    @Test
    public void booleanSchema() {
        BooleanSchema actual = (BooleanSchema) SchemaLoader.load(get("booleanSchema"));
        assertNotNull(actual);
    }

    @Test
    public void builderhasDefaultFormatValidators() {
        SchemaLoader actual = SchemaLoader.builder().schemaJson(get("booleanSchema")).build();
        assertTrue(actual.getFormatValidator("date-time").get() instanceof DateTimeFormatValidator);
        assertTrue(actual.getFormatValidator("uri").get() instanceof URIV4FormatValidator);
        assertTrue(actual.getFormatValidator("email").get() instanceof EmailFormatValidator);
        assertTrue(actual.getFormatValidator("ipv4").get() instanceof IPV4Validator);
        assertTrue(actual.getFormatValidator("ipv6").get() instanceof IPV6Validator);
        assertTrue(actual.getFormatValidator("hostname").get() instanceof HostnameFormatValidator);
    }

    @Test
    public void builderUsesDefaultSchemaClient() {
        SchemaLoaderBuilder actual = SchemaLoader.builder();
        assertNotNull(actual);
        assertTrue(actual.httpClient instanceof DefaultSchemaClient);
    }

    @Test
    public void customFormat() {
        Schema subject = SchemaLoader.builder()
                .schemaJson(get("customFormat"))
                .addFormatValidator("custom", obj -> Optional.of("failure"))
                .build().load().build();
        TestSupport.expectFailure(subject, "asd");
    }

    @Test
    public void emptyPatternProperties() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("emptyPatternProperties"));
        assertNotNull(actual);
        assertEquals(0, actual.getPatternProperties().size());
    }

    @Test
    public void emptySchema() {
        assertTrue(SchemaLoader.load(get("emptySchema")) instanceof EmptySchema);
    }

    @Test
    public void emptySchemaWithDefault() {
        EmptySchema actual = (EmptySchema) SchemaLoader.load(get("emptySchemaWithDefault"));
        assertNotNull(actual);
    }

    @Test
    public void enumSchema() {
        EnumSchema actual = (EnumSchema) SchemaLoader.load(get("enumSchema"));
        Map<String, Object> expectedObject = new HashMap<>();
        expectedObject.put("a", "b");
        assertEquals(new HashSet<>(asList(1, 2, "a", expectedObject, null)), new HashSet<>(actual.getPossibleValues()));
    }

    @Test
    public void genericProperties() {
        Schema actual = SchemaLoader.load(get("genericProperties"));
        assertEquals("myId", actual.getId());
        assertEquals("my title", actual.getTitle());
        assertEquals("my description", actual.getDescription());
    }

    @Test
    public void integerSchema() {
        NumberSchema actual = (NumberSchema) SchemaLoader.load(get("integerSchema"));
        assertEquals(10, actual.getMinimum().intValue());
        assertEquals(20, actual.getMaximum().intValue());
        assertEquals(5, actual.getMultipleOf().intValue());
        assertTrue(actual.isExclusiveMinimum());
        assertTrue(actual.isExclusiveMaximum());
        assertTrue(actual.requiresInteger());
    }

    @Test
    public void conditionalSchemaIf() {
        ConditionalSchema actual = (ConditionalSchema) loadAsV7(get("conditionalSchemaIf"));
        Assert.assertTrue(actual.getIfSchema().isPresent());
        Assert.assertFalse(actual.getThenSchema().isPresent());
        Assert.assertFalse(actual.getElseSchema().isPresent());
    }

    @Test
    public void conditionalSchemaThen() {
        ConditionalSchema actual = (ConditionalSchema) loadAsV7(get("conditionalSchemaThen"));
        Assert.assertFalse(actual.getIfSchema().isPresent());
        Assert.assertTrue(actual.getThenSchema().isPresent());
        Assert.assertFalse(actual.getElseSchema().isPresent());
    }

    @Test
    public void conditionalSchemaElse() {
        ConditionalSchema actual = (ConditionalSchema) loadAsV7(get("conditionalSchemaElse"));
        Assert.assertFalse(actual.getIfSchema().isPresent());
        Assert.assertFalse(actual.getThenSchema().isPresent());
        Assert.assertTrue(actual.getElseSchema().isPresent());
    }

    @Test
    public void conditionalSchemaIfThenElse() {
        ConditionalSchema actual = (ConditionalSchema) loadAsV7(get("conditionalSchemaIfThenElse"));
        Assert.assertTrue(actual.getIfSchema().isPresent());
        Assert.assertTrue(actual.getThenSchema().isPresent());
        Assert.assertTrue(actual.getElseSchema().isPresent());
    }

    @Test
    public void conditionalSchemaLoadingV4() {
        Schema actual = SchemaLoader.load(get("conditionalSchemaIf"));
        assertFalse(actual instanceof ConditionalSchema);
    }

    @Test
    public void conditionalSchemaLoadingV6() {
        Schema actual = loadAsV6(get("conditionalSchemaIf"));
        assertFalse(actual instanceof ConditionalSchema);
    }

    @Test
    public void conditionalSchemaIfSubSchemaTrue() {
        ConditionalSchema actual = (ConditionalSchema) loadAsV7(get("conditionalSchemaIfSubSchemaTrue"));
        Assert.assertTrue(actual.getIfSchema().isPresent());
        Assert.assertFalse(actual.getThenSchema().isPresent());
        Assert.assertFalse(actual.getElseSchema().isPresent());
        assertTrue(actual.getIfSchema().get() instanceof TrueSchema);
    }

    @Test(expected = SchemaException.class)
    public void invalidExclusiveMinimum() {
        SchemaLoader.load(get("invalidExclusiveMinimum"));
    }

    @Test(expected = SchemaException.class)
    public void invalidIntegerSchema() {
        JSONObject input = get("invalidIntegerSchema");
        SchemaLoader.load(input);
    }

    @Test(expected = SchemaException.class)
    public void invalidStringSchema() {
        SchemaLoader.load(get("invalidStringSchema"));
    }

    @Test(expected = SchemaException.class)
    public void invalidType() {
        SchemaLoader.load(get("invalidType"));
    }

    @Test
    public void jsonPointerInArray() {
        assertTrue(SchemaLoader.load(get("jsonPointerInArray")) instanceof ArraySchema);
    }

    @Test
    public void multipleTypes() {
        assertTrue(SchemaLoader.load(get("multipleTypes")) instanceof CombinedSchema);
    }

    @Test
    public void implicitAnyOfLoadsTypeProps() {
        CombinedSchema schema = (CombinedSchema) SchemaLoader.load(get("multipleTypesWithProps"));
        StringSchema stringSchema = schema.getSubschemas().stream()
                .filter(sub -> sub instanceof StringSchema)
                .map(sub -> (StringSchema) sub)
                .findFirst().orElseThrow(() -> new AssertionError("no StringSchema"));
        NumberSchema numSchema = schema.getSubschemas().stream()
                .filter(sub -> sub instanceof NumberSchema)
                .map(sub -> (NumberSchema) sub)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no NumberSchema"));
        assertEquals(3, stringSchema.getMinLength().intValue());
        assertEquals(5, numSchema.getMinimum().intValue());
    }

    @Test
    public void neverMatchingAnyOf() {
        assertTrue(SchemaLoader.load(get("anyOfNeverMatches")) instanceof CombinedSchema);
    }

    @Test
    public void noExplicitObject() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("noExplicitObject"));
        assertFalse(actual.requiresObject());
    }

    @Test
    public void notSchema() {
        NotSchema actual = (NotSchema) SchemaLoader.load(get("notSchema"));
        assertNotNull(actual);
    }

    @Test
    public void nullSchema() {
        NullSchema actual = (NullSchema) SchemaLoader.load(get("nullSchema"));
        assertNotNull(actual);
    }

    @Test
    public void pointerResolution() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("pointerResolution"));
        ObjectSchema rectangleSchema = (ObjectSchema) ((ReferenceSchema) actual.getPropertySchemas()
                .get("rectangle"))
                .getReferredSchema();
        assertNotNull(rectangleSchema);
        ReferenceSchema aRef = (ReferenceSchema) rectangleSchema.getPropertySchemas().get("a");
        assertTrue(aRef.getReferredSchema() instanceof NumberSchema);
    }

    @Test
    public void pointerResolvedToBoolean() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("pointerResolution"));
        TrueSchema boolSchema = (TrueSchema) ((ReferenceSchema) actual.getPropertySchemas()
                .get("boolRef"))
                .getReferredSchema();

        assertNotNull(boolSchema);
    }

    @Test
    public void v6InternalRefResolution() {
        SchemaLoader loader = v6Loader().schemaJson(get("v6Ref")).build();
        Schema actual = loader.load().build();
    }

    @Test
    public void sameDocumentReferenceResolution() {
        v6Loader().schemaJson(get("v6SameDocumentRef")).build().load().build();
    }

    @Test(expected = SchemaException.class)
    public void pointerResolutionFailure() {
        SchemaLoader.load(get("pointerResolutionFailure"));
    }

    @Test(expected = SchemaException.class)
    public void pointerResolutionQueryFailure() {
        SchemaLoader.load(get("pointerResolutionQueryFailure"));
    }

    @Test @Ignore
    public void propsAroundRefExtendTheReferredSchema() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader
                .load(get("propsAroundRefExtendTheReferredSchema"));
        ReferenceSchema propRef = (ReferenceSchema) actual.getPropertySchemas().get("prop");
        ObjectSchema prop = (ObjectSchema) propRef
                .getReferredSchema();
        assertTrue(prop.requiresObject());
        assertEquals(1, prop.getMinProperties().intValue());
    }

    @Test
    public void recursiveSchema() {
        SchemaLoader.load(get("recursiveSchema"));
    }

    @Test
    public void refWithType() {
        ObjectSchema actualRoot = (ObjectSchema) SchemaLoader.load(get("refWithType"));
        ReferenceSchema actual = (ReferenceSchema) actualRoot.getPropertySchemas().get("prop");
        ObjectSchema propSchema = (ObjectSchema) actual.getReferredSchema();
        assertEquals(propSchema.getRequiredProperties(), asList("a", "b"));
    }

    @Test
    public void remotePointerResulion() {
        SchemaClient httpClient = mock(SchemaClient.class);
        when(httpClient.get("http://example.org/asd")).thenReturn(asStream("{}"));
        when(httpClient.get("http://example.org/otherschema.json")).thenReturn(asStream("{}"));
        when(httpClient.get("http://example.org/folder/subschemaInFolder.json")).thenReturn(
                asStream("{}"));
        SchemaLoader.load(get("remotePointerResolution"), httpClient);
    }

    @Test
    public void resolutionScopeTest() {
        SchemaLoader.load(get("resolutionScopeTest"), new SchemaClient() {

            @Override
            public InputStream get(final String url) {
                System.out.println("GET " + url);
                return new DefaultSchemaClient().get(url);
            }
        });
    }

    @Test
    public void selfRecursiveSchema() {
        SchemaLoader.load(get("selfRecursiveSchema"));
    }

    @Test
    public void sniffByFormat() {
        JSONObject schema = new JSONObject();
        schema.put("format", "hostname");
        Schema actual = SchemaLoader.builder().schemaJson(schema).build().load().build();
        assertTrue(actual instanceof StringSchema);
    }

    @Test
    public void sniffByContains() {
        JSONObject schema = new JSONObject();
        schema.put("contains", new JSONObject());
        Schema actual = loadAsV6(schema);
        assertTrue(actual instanceof ArraySchema);
    }

    @Test
    public void sniffByContainsDoesNotAffectV4() {
        JSONObject schema = new JSONObject();
        schema.put("contains", new JSONObject());
        Schema actual = SchemaLoader.load(schema);
        assertFalse(actual instanceof ArraySchema);
    }

    @Test
    public void trueIsTrueSchema() {
        assertTrue(loadAsV6(true) instanceof TrueSchema);
    }

    @Test
    public void falseIsFalseSchema() {
        assertTrue(loadAsV6(false) instanceof FalseSchema);
    }

    @Test
    public void stringSchema() {
        StringSchema actual = (StringSchema) SchemaLoader.load(get("stringSchema"));
        assertEquals(2, actual.getMinLength().intValue());
        assertEquals(3, actual.getMaxLength().intValue());
    }

    @Test
    public void stringSchemaWithFormat() {
        StringSchema subject = (StringSchema) SchemaLoader.load(get("stringSchemaWithFormat"));
        TestSupport.expectFailure(subject, "asd");
    }

    @Test
    public void tupleSchema() {
        ArraySchema actual = (ArraySchema) SchemaLoader.load(get("tupleSchema"));
        assertFalse(actual.permitsAdditionalItems());
        assertNull(actual.getAllItemSchema());
        assertEquals(2, actual.getItemSchemas().size());
        assertEquals(BooleanSchema.INSTANCE, actual.getItemSchemas().get(0));
        assertEquals(NullSchema.INSTANCE, actual.getItemSchemas().get(1));
    }

    @Test(expected = SchemaException.class)
    public void unknownSchema() {
        SchemaLoader.load(get("unknown"));
    }

    @Test
    public void unsupportedFormat() {
        JSONObject schema = new JSONObject();
        schema.put("type", "string");
        schema.put("format", "unknown");
        SchemaLoader.builder().schemaJson(schema).build().load();
    }

    @Test
    public void schemaJsonIdIsRecognized() {
        SchemaClient client = mock(SchemaClient.class);
        ByteArrayInputStream retval = new ByteArrayInputStream("{}".getBytes());
        when(client.get("http://example.org/schema/schema.json")).thenReturn(retval);
        SchemaLoader.builder().schemaJson(get("schemaWithId"))
                .httpClient(client)
                .build().load();
    }

    @Test
    public void v6SchemaJsonIdIsRecognized() {
        SchemaClient client = mock(SchemaClient.class);
        ByteArrayInputStream retval = new ByteArrayInputStream("{}".getBytes());
        when(client.get("http://example.org/schema/schema.json")).thenReturn(retval);
        v6Loader()
                .schemaJson(get("schemaWithIdV6"))
                .httpClient(client)
                .build().load();
    }

    @Test
    public void withoutFragment() {
        String actual = ReferenceLookup.withoutFragment("http://example.com#frag").toString();
        assertEquals("http://example.com", actual);
    }

    @Test
    public void withoutFragmentNoFragment() {
        String actual = ReferenceLookup.withoutFragment("http://example.com").toString();
        assertEquals("http://example.com", actual);
    }

    @Test
    public void toOrgJSONObject() {
        JSONObject orig = new JSONObject("{\"a\":{\"b\":1}}");
        JSONObject actual = SchemaLoader.toOrgJSONObject((JsonObject) JsonValue.of(orig));
        assertEquals(orig.toString(), actual.toString());
    }

    @Test
    public void schemaPointerIsPopulated() {
        JSONObject rawSchema = ResourceLoader.DEFAULT.readObj("objecttestschemas.json")
                .getJSONObject("objectWithSchemaDep");
        ObjectSchema schema = (ObjectSchema) SchemaLoader.load(rawSchema);

        String actualSchemaPointer = schema.getSchemaDependencies().get("a").getSchemaLocation();
        String expectedSchemaPointer = new JSONPointer(asList("dependencies", "a")).toURIFragment();
        assertEquals(expectedSchemaPointer, actualSchemaPointer);
    }

    @Test
    public void constLoading() {
        Schema actual = loadAsV6(get("constSchema"));
        assertTrue(actual instanceof ConstSchema);
    }

    @Test
    public void constLoadingV4() {
        Schema actual = SchemaLoader.load(get("constSchema"));
        assertFalse(actual instanceof ConstSchema);
    }

    @Test
    public void automaticSchemaVersionRecognition() {
        SchemaLoader loader = SchemaLoader.builder()
                .schemaJson(get("explicitSchemaVersion"))
                .build();
        assertEquals(DRAFT_6, loader.specVersion());
    }

    @Test
    public void folderNameResolution() {
        SchemaClient client = mock(SchemaClient.class);
        when(client.get("http://localhost/folder/Identifier.json")).thenReturn(asStream("{}"));
        v6Loader().schemaJson(get("folderNameResolution"))
                .httpClient(client).build().load().build();

    }

    @Test
    public void otheFolderNameResolution() {
        v6Loader().schemaJson(get("otherFolderNameResolution")).build().load().build();
    }

    @Test
    public void refRemoteV4() {
        SchemaClient httpClient = mock(SchemaClient.class);
        when(httpClient.get("http://localhost:1234/folder/folderInteger.json")).thenReturn(asStream("{}"));

        SchemaLoader.builder().httpClient(httpClient)
                .schemaJson(get("refRemoteV4"))
                .build()
                .load().build();
    }

    @Test
    public void refPointerDerivatedFromPointer() {
        SchemaClient httpClient = mock(SchemaClient.class);
        when(httpClient.get("http://localhost:1234/folder/folderInteger.json")).thenReturn(asStream("{}"));

        SchemaLoader.builder().httpClient(httpClient)
                .schemaJson(get("refPointerDerivatedFromPointer"))
                .build()
                .load().build();
    }

    @Test
    public void relativeIdInReferencedSchemaRoot() {
        SchemaClient httpClient = mock(SchemaClient.class);
        when(httpClient.get("http://localhost:1234/folder/folderInteger.json")).thenReturn(asStream("{}"));

        SchemaLoader.builder().httpClient(httpClient)
                .schemaJson(get("relativeIdInReferencedSchemaRoot"))
                .build()
                .load().build();
    }

    @Test
    public void applyDefaultNumberTest() {
        JSONObject rawSchema = ALL_SCHEMAS.getJSONObject("defaultsTest");
        ObjectSchema schema = (ObjectSchema) SchemaLoader
                .builder()
                .useDefaults(true)
                .schemaJson(rawSchema)
                .build().load().build();

        JSONObject obj = new JSONObject();
        schema.validate(obj);

        assertEquals(10, obj.getNumber("numberDefault"));
    }

    @Test
    public void applyDefaultStringTest() {
        JSONObject rawSchema = ALL_SCHEMAS.getJSONObject("defaultsTest");
        ObjectSchema schema = (ObjectSchema) SchemaLoader
                .builder()
                .useDefaults(true)
                .schemaJson(rawSchema)
                .build().load().build();

        JSONObject obj = new JSONObject();
        schema.validate(obj);

        assertEquals("yee", obj.getString("stringDefault"));
    }

    @Test
    public void applyDefaultBooleanTest() {
        JSONObject rawSchema = ALL_SCHEMAS.getJSONObject("defaultsTest");
        ObjectSchema schema = (ObjectSchema) SchemaLoader
                .builder()
                .useDefaults(true)
                .schemaJson(rawSchema)
                .build().load().build();

        JSONObject obj = new JSONObject();
        schema.validate(obj);

        assertEquals(true, obj.getBoolean("booleanDefault"));
    }

    @Test
    public void applyDefaultObjectTest() {
        JSONObject rawSchema = ALL_SCHEMAS.getJSONObject("defaultsTest");
        ObjectSchema schema = (ObjectSchema) SchemaLoader
                .builder()
                .useDefaults(true)
                .schemaJson(rawSchema)
                .build().load().build();

        JSONObject obj = new JSONObject();
        schema.validate(obj);

        assertEquals(new JSONObject("{\"hello\": \"world\"}").toString(), obj.getJSONObject("objectDefault").toString());
    }

    @Test
    public void applyDefaultArrayTest() {
        JSONObject rawSchema = ALL_SCHEMAS.getJSONObject("defaultsTest");
        ObjectSchema schema = (ObjectSchema) SchemaLoader
                .builder()
                .useDefaults(true)
                .schemaJson(rawSchema)
                .build().load().build();

        JSONObject obj = new JSONObject();
        schema.validate(obj);

        assertEquals(new JSONArray("[\"a\",\"b\",\"c\"]").toString(), obj.getJSONArray("arrayDefault").toString());
    }

    @Test
    public void applyDefaultNullTest() {
        JSONObject rawSchema = ALL_SCHEMAS.getJSONObject("defaultsTest");
        ObjectSchema schema = (ObjectSchema) SchemaLoader
                .builder()
                .useDefaults(true)
                .schemaJson(rawSchema)
                .build().load().build();

        JSONObject obj = new JSONObject();
        schema.validate(obj);

        assertEquals(JSONObject.NULL, obj.get("nullDefault"));
    }

    @Test
    public void nullableBooleansAre_Loaded_withNullableSupport() {
        SchemaLoader loader = SchemaLoader.builder()
                .nullableSupport(true)
                .schemaJson(get("nullableSupport"))
                .build();
        ObjectSchema actual = (ObjectSchema) loader.load().build();
        Schema nullableSchema = actual.getPropertySchemas().get("isNullable");
        Schema nonNullableSchema = actual.getPropertySchemas().get("nonNullable");
        Schema implicitNonNullable = actual.getPropertySchemas().get("implicitNonNullable");

        assertTrue(nullableSchema.isNullable());
        assertFalse(nonNullableSchema.isNullable());
        assertFalse(implicitNonNullable.isNullable());
    }

    @Test
    public void nullableBooleansAre_NotLoaded_withoutNullableSupport() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("nullableSupport"));
        Schema nullableSchema = actual.getPropertySchemas().get("isNullable");
        Schema nonNullableSchema = actual.getPropertySchemas().get("nonNullable");
        Schema implicitNonNullable = actual.getPropertySchemas().get("implicitNonNullable");

        assertNull(nullableSchema.isNullable());
        assertNull(nonNullableSchema.isNullable());
        assertNull(implicitNonNullable.isNullable());
    }
}
