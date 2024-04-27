package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.everit.json.schema.TestSupport.asStream;
import static org.everit.json.schema.TestSupport.loadAsV6;
import static org.everit.json.schema.TestSupport.loadAsV7;
import static org.everit.json.schema.TestSupport.v6Loader;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.everit.json.schema.*;
import org.everit.json.schema.internal.DateTimeFormatValidator;
import org.everit.json.schema.internal.EmailFormatValidator;
import org.everit.json.schema.internal.HostnameFormatValidator;
import org.everit.json.schema.internal.IPV4Validator;
import org.everit.json.schema.internal.IPV6Validator;
import org.everit.json.schema.internal.URIV4FormatValidator;
import org.everit.json.schema.loader.SchemaLoader.SchemaLoaderBuilder;
import org.everit.json.schema.loader.internal.DefaultProviderValidators;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SchemaLoaderTest {

    private static JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("testschemas.json");

    private static JSONObject get(String schemaName) {
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
    public void builderOverrideOfBuiltInFormatValidators() {
        SchemaLoader actual = SchemaLoader.builder().schemaJson(get("booleanSchema"))
                .addFormatValidator(new CustomDateTimeFormatValidator())
                .enableOverrideOfBuiltInFormatValidators()
                .build();
        assertTrue(actual.getFormatValidator("date-time").get() instanceof CustomDateTimeFormatValidator);
    }

    @Test
    public void builderKeepBuiltInFormatValidatorsByDefault() {
        SchemaLoader actual = SchemaLoader.builder().schemaJson(get("booleanSchema"))
                .addFormatValidator(new CustomDateTimeFormatValidator())
                .build();
        assertTrue(actual.getFormatValidator("date-time").get() instanceof DateTimeFormatValidator);
    }

    @Test
    public void builderUsesDefaultSchemaClient() {
        SchemaLoaderBuilder actual = SchemaLoader.builder();
        assertNotNull(actual);
        assertTrue(actual.schemaClient instanceof DefaultSchemaClient);
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
    public void dynamicFormat() {
        Schema subject = SchemaLoader.builder(new ExampleDefaultProviderValidators())
                .schemaJson(get("dynamicFormat"))
                .build().load().build();
        TestSupport.expectFailure(subject, "6");
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
        assertTrue(actual.getIfSchema().isPresent());
        assertFalse(actual.getThenSchema().isPresent());
        assertFalse(actual.getElseSchema().isPresent());
    }

    @Test
    public void conditionalSchemaThen() {
        ConditionalSchema actual = (ConditionalSchema) loadAsV7(get("conditionalSchemaThen"));
        assertFalse(actual.getIfSchema().isPresent());
        assertTrue(actual.getThenSchema().isPresent());
        assertFalse(actual.getElseSchema().isPresent());
    }

    @Test
    public void conditionalSchemaElse() {
        ConditionalSchema actual = (ConditionalSchema) loadAsV7(get("conditionalSchemaElse"));
        assertFalse(actual.getIfSchema().isPresent());
        assertFalse(actual.getThenSchema().isPresent());
        assertTrue(actual.getElseSchema().isPresent());
    }

    @Test
    public void conditionalSchemaIfThenElse() {
        ConditionalSchema actual = (ConditionalSchema) loadAsV7(get("conditionalSchemaIfThenElse"));
        assertTrue(actual.getIfSchema().isPresent());
        assertTrue(actual.getThenSchema().isPresent());
        assertTrue(actual.getElseSchema().isPresent());
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
        assertTrue(actual.getIfSchema().isPresent());
        assertFalse(actual.getThenSchema().isPresent());
        assertFalse(actual.getElseSchema().isPresent());
        assertTrue(actual.getIfSchema().get() instanceof TrueSchema);
    }

    @Test
    public void invalidExclusiveMinimum() {
        Assertions.assertThrows(SchemaException.class, () -> {
            SchemaLoader.load(get("invalidExclusiveMinimum"));
        });
    }

    @Test
    public void invalidIntegerSchema() {
        Assertions.assertThrows(SchemaException.class, () -> {
            JSONObject input = get("invalidIntegerSchema");
            SchemaLoader.load(input);
        });
    }

    @Test
    public void invalidStringSchema() {
        Assertions.assertThrows(SchemaException.class, () -> {
            SchemaLoader.load(get("invalidStringSchema"));
        });
    }

    @Test
    public void invalidType() {
        Assertions.assertThrows(SchemaException.class, () -> {
            SchemaLoader.load(get("invalidType"));
        });
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

    @Test
    public void pointerResolutionFailure() {
        Assertions.assertThrows(SchemaException.class, () -> {
            SchemaLoader.load(get("pointerResolutionFailure"));
        });
    }

    @Test
    public void pointerResolutionQueryFailure() {
        Assertions.assertThrows(SchemaException.class, () -> {
            SchemaLoader.load(get("pointerResolutionQueryFailure"));
        });
    }

    @Test
    void multipleOfShouldNotBeZero() {
        SchemaException thrown = Assertions.assertThrows(SchemaException.class, () -> {
            SchemaLoader.load(get("multipleOfShouldNotBeZero"));
        });
        assertEquals("#: multipleOf should not be 0", thrown.getMessage());
    }

    @Test @Disabled
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
        SchemaClient schemaClient = mock(SchemaClient.class);
        when(schemaClient.get("http://example.org/asd")).thenReturn(asStream("{}"));
        when(schemaClient.get("http://example.org/otherschema.json")).thenReturn(asStream("{}"));
        when(schemaClient.get("http://example.org/folder/subschemaInFolder.json")).thenReturn(
                asStream("{}"));
        SchemaLoader.load(get("remotePointerResolution"), schemaClient);
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

    @Test
    public void unknownSchema() {
        Assertions.assertThrows(SchemaException.class, () -> {
            SchemaLoader.load(get("unknown"));
        });
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
                .schemaClient(client)
                .build().load();
    }

    @Test
    public void v6SchemaJsonIdIsRecognized() {
        SchemaClient client = mock(SchemaClient.class);
        ByteArrayInputStream retval = new ByteArrayInputStream("{}".getBytes());
        when(client.get("http://example.org/schema/schema.json")).thenReturn(retval);
        v6Loader()
                .schemaJson(get("schemaWithIdV6"))
                .schemaClient(client)
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
        SchemaLoaderBuilder builder = SchemaLoader.builder();
        SchemaLoader loader = builder.schemaJson(get("explicitSchemaVersion")).build();
        assertEquals(DRAFT_6.defaultFormatValidators(), builder.providerValidators.getFormatValidators());
        assertEquals(DRAFT_6, loader.specVersion());
    }

    @Test
    public void folderNameResolution() {
        SchemaClient client = mock(SchemaClient.class);
        when(client.get("http://localhost/folder/Identifier.json")).thenReturn(asStream("{}"));
        v6Loader().schemaJson(get("folderNameResolution"))
                .schemaClient(client).build().load().build();

    }

    @Test
    public void otherFolderNameResolution() {
        v6Loader().schemaJson(get("otherFolderNameResolution")).build().load().build();
    }

    @Test
    public void refRemoteV4() {
        SchemaClient schemaClient = mock(SchemaClient.class);
        when(schemaClient.get("http://localhost:1234/folder/folderInteger.json")).thenReturn(asStream("{}"));

        SchemaLoader.builder().schemaClient(schemaClient)
                .schemaJson(get("refRemoteV4"))
                .build()
                .load().build();
    }

    @Test
    public void refPointerDerivatedFromPointer() {
        SchemaClient schemaClient = mock(SchemaClient.class);
        when(schemaClient.get("http://localhost:1234/folder/folderInteger.json")).thenReturn(asStream("{}"));

        SchemaLoader.builder().schemaClient(schemaClient)
                .schemaJson(get("refPointerDerivatedFromPointer"))
                .build()
                .load().build();
    }

    @Test
    public void relativeIdInReferencedSchemaRoot() {
        SchemaClient schemaClient = mock(SchemaClient.class);
        when(schemaClient.get("http://localhost:1234/folder/folderInteger.json")).thenReturn(asStream("{}"));

        SchemaLoader.builder().schemaClient(schemaClient)
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

    @Test
    public void unknownTypeException() {
        try {
            SchemaLoader.load(get("unknownType"));
            fail("did not throw exception for unknown type");
        } catch (SchemaException actual) {
            SchemaException expected = new SchemaException("#/properties/prop", "unknown type: [integgggger]");
            assertEquals(expected, actual);
        }
    }

    @Test
    public void unknownMetaSchemaException() {
        try {
            SchemaLoader.load(get("unknownMetaSchema"));
            fail("did not throw exception");
        } catch (SchemaException e) {
            assertEquals("#", e.getSchemaLocation());
        }
    }

    @Test
    public void syntheticAllOf() {
        String actual = SchemaLoader.load(get("boolAndNot")).toString();

        assertThat(new JSONObject(actual), sameJsonAs(get("boolAndNot")));
    }

    @Test
    public void syntheticAllOfTrue() {
        JSONObject o = get("trueAndNot");
        String actual = SchemaLoader.load(o).toString();
        assertThat(new JSONObject(actual), sameJsonAs(o));
    }

    @Test
    public void commonPropsGoIntoWrappingAllOf() {
        CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("syntheticAllOfWithCommonProps"));
        assertEquals(CombinedSchema.ALL_CRITERION, actual.getCriterion());
        assertEquals("http://id", actual.getId());
        assertEquals("my title", actual.getTitle());
        assertEquals("my description", actual.getDescription());
        assertNull(actual.getSubschemas().iterator().next().getId());
    }

    @Test
    public void unprocessedPropertiesAreLoaded() {
        SchemaLoader loader = SchemaLoader.builder()
                .draftV7Support()
                .useDefaults(true)
                .schemaJson(get("schemaWithUnprocessedProperties"))
                .build();
        ObjectSchema actual = (ObjectSchema) loader.load().build();

        assertEquals(ImmutableMap.of(
                "unproc0", 1,
                "unproc1", "asdasd",
                "nullable", false
        ), actual.getUnprocessedProperties());
        assertEquals(emptyMap(), actual.getPropertySchemas().get("prop").getUnprocessedProperties());
        assertEquals(ImmutableMap.of(
                "unproc4", true,
                "unproc5", JSONObject.NULL
        ), actual.getPropertySchemas().get("prop2").getUnprocessedProperties());
        assertEquals(ImmutableMap.of(
                "unproc6", false
        ), actual.getPropertySchemas().get("prop3").getUnprocessedProperties());
    }

    @Test
    public void commonPropsAreNotUnprocessedProps() {
        JSONObject schemaJson = get("schemaWithUnprocessedProperties");
        ObjectSchema subject = (ObjectSchema) SchemaLoader.load(schemaJson);
        assertThat(new JSONObject(subject.toString()), sameJsonAs(schemaJson));
    }

    @Test
    public void unprocessedPropertiesAreLoadedForRefElement() {
        SchemaLoader loader =
                SchemaLoader.builder()
                        .draftV7Support()
                        .useDefaults(true)
                        .schemaJson(get("schemaRefWithUnprocessedProperties"))
                        .build();
        ObjectSchema actual = (ObjectSchema) loader.load().build();

        assertEquals(ImmutableMap.of(
                "unproc6", false
        ), actual.getPropertySchemas().get("prop3").getUnprocessedProperties());

        assertEquals(
                ImmutableMap.of("unproc8", false),
                ((ReferenceSchema) actual.getPropertySchemas().get("prop4"))
                        .getReferredSchema()
                        .getUnprocessedProperties());

        assertEquals(
                ImmutableMap.of("unproc4", true, "unproc5", JSONObject.NULL),
                actual.getPropertySchemas().get("prop2").getUnprocessedProperties());

        assertEquals(
                ImmutableMap.of("unproc7", JSONObject.NULL),
                actual.getPropertySchemas().get("prop4").getUnprocessedProperties());

        assertEquals(
                ImmutableMap.of("unproc8", false),
                ((ReferenceSchema) actual.getPropertySchemas().get("prop4")).getReferredSchema().getUnprocessedProperties());

        assertEquals(
                ImmutableMap.of("unproc9", ImmutableMap.of("unproc9-01", false)),
                actual.getPropertySchemas().get("prop5").getUnprocessedProperties());

        assertEquals(
                ImmutableMap.of("unproc8", false),
                ((ReferenceSchema) actual.getPropertySchemas().get("prop5")).getReferredSchema().getUnprocessedProperties());
    }

    @Test
    public void httpsSchemaURI() {
        JSONObject schemaJson = ResourceLoader.DEFAULT.readObj("https-schema-uri.json");
        Schema schema = SchemaLoader.load(schemaJson);
        assertNotNull(schema.getSchemaLocation());
    }

    private class CustomDateTimeFormatValidator extends DateTimeFormatValidator {}

    @Test
    public void explicitlySetResolutionScope_isMappedToSchemaLocation() throws URISyntaxException {
        Schema actual = SchemaLoader.builder()
            .resolutionScope("http://example.org")
            .schemaJson(new JSONObject("{}"))
            .build().load().build();
        assertEquals(new SchemaLocation(new URI("http://example.org"), emptyList()), actual.getLocation());
    }


    private class ExampleDefaultProviderValidators extends DefaultProviderValidators {
        @Override
        public FormatValidator getFormatValidator(String formatName) {

            if (!this.getFormatValidators().containsKey(formatName)
                    && formatName.startsWith("javascript:")) {
                String script = formatName.substring(formatName.lastIndexOf("javascript:"),formatName.length());
                this.addFormatValidator(formatName, new JavascriptFormatValidator(formatName, script));
            }
            return super.getFormatValidator(formatName);
        }
    }

    private class JavascriptFormatValidator implements FormatValidator {

        String script;
        String formatName;

        public JavascriptFormatValidator(String formatName, String script) {
            this.formatName = formatName;
            this.script = script;
        }

        @Override
        public Optional<String> validate(String subject) {

            ScriptEngine javaScriptEngine = new ScriptEngineManager().getEngineByName("js");
            javaScriptEngine.put("subject",subject);
            try {
                Boolean result = (Boolean) javaScriptEngine.eval(script);
                if (!result) {
                    return Optional.of(String.format("the length of string [%s] is  not equal 5", subject));
                }
            } catch (ScriptException e) {
                e.printStackTrace();
                return Optional.of(String.format("Error on evalutation of [%s] ", subject));
            }
            return Optional.empty();
        }

        @Override
        public String formatName() {
            return formatName;
        }
    }


}
