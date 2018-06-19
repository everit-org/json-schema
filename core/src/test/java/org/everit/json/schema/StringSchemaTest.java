package org.everit.json.schema;

import static org.everit.json.schema.ObjectComparator.deepEquals;
import static org.everit.json.schema.TestSupport.buildWithLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.regex.Pattern;

import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.JsonValue;
import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.regexp.JavaUtilRegexpFactory;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class StringSchemaTest {

    private static Schema loadWithNullableSupport(JsonObject rawSchemaJson) {
        return SchemaLoader.builder().nullableSupport(true).schemaJson(rawSchemaJson).build().load().build();
    }

    @Test
    public void formatFailure() {
        StringSchema subject = buildWithLocation(StringSchema.builder()
                .formatValidator(subj -> Optional.of("violation")));
        TestSupport.failureOf(subject)
                .expectedKeyword("format")
                .input("string")
                .expect();
    }

    @Test
    public void formatSuccess() {
        StringSchema subject = StringSchema.builder().formatValidator(subj -> Optional.empty()).build();
        subject.validate("string");
    }

    @Test
    public void maxLength() {
        StringSchema subject = buildWithLocation(StringSchema.builder().maxLength(3));
        TestSupport.failureOf(subject)
                .expectedKeyword("maxLength")
                .input("foobar")
                .expect();
    }

    @Test
    public void minLength() {
        StringSchema subject = buildWithLocation(StringSchema.builder().minLength(2));
        TestSupport.failureOf(subject)
                .expectedKeyword("minLength")
                .input("a")
                .expect();
    }

    @Test
    public void multipleViolations() {
        try {
            StringSchema.builder().minLength(3).maxLength(1).pattern("^b.*").build().validate("ab");
            Assert.fail();
        } catch (ValidationException e) {
            Assert.assertEquals(3, e.getCausingExceptions().size());
        }
    }

    @Test
    public void notRequiresString() {
        StringSchema.builder().requiresString(false).build().validate(2);
    }

    @Test
    public void patternFailure() {
        StringSchema subject = buildWithLocation(StringSchema.builder().pattern("^a*$"));
        TestSupport.failureOf(subject).expectedKeyword("pattern").input("abc").expect();
    }

    @Test
    public void patternSuccess() {
        StringSchema.builder().pattern("^a*$").build().validate("aaaa");
    }

    @Test
    public void success() {
        StringSchema.builder().build().validate("foo");
    }

    @Test
    public void typeFailure() {
        TestSupport.failureOf(StringSchema.builder())
                .expectedKeyword("type")
                .input(null)
                .expect();
    }

    @Test(expected = ValidationException.class)
    public void issue38Pattern() {
        StringSchema.builder().requiresString(true).pattern("\\+?\\d+").build().validate("aaa");
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(StringSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .withPrefabValues(Pattern.class, Pattern.compile("red"), Pattern.compile("black"))
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() {
        JsonObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        JsonNode actualNode = JsonSchemaUtil.stringToNode(actual);
        JsonValue actualJsonObject = JsonValue.of(actualNode);
        assertTrue(deepEquals(rawSchemaJson, actualJsonObject));
    }

    @Test
    public void toStringWithNullableTrueTest() {
    	JsonObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.put("nullable", true);
        String actual = loadWithNullableSupport(rawSchemaJson).toString();
        JsonNode actualNode = JsonSchemaUtil.stringToNode(actual);
        JsonValue actualJsonObject = JsonValue.of(actualNode);
        assertTrue(deepEquals(rawSchemaJson, actualJsonObject));
    }

    @Test
    public void toStringWithNullableFalseTest() {
    	JsonObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.put("nullable", false);
        String actual = loadWithNullableSupport(rawSchemaJson).toString();
        JsonNode actualNode = JsonSchemaUtil.stringToNode(actual);
        JsonValue actualJsonObject = JsonValue.of(actualNode);
        assertTrue(deepEquals(rawSchemaJson, actualJsonObject));
    }

    @Test
    public void toStringNoFormat() {
    	JsonObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.remove("format");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        JsonNode actualNode = JsonSchemaUtil.stringToNode(actual);
        JsonValue actualJsonObject = JsonValue.of(actualNode);
        assertTrue(deepEquals(rawSchemaJson, actualJsonObject));
    }

    @Test
    public void toStringNoExplicitType() {
    	JsonObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.remove("type");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        JsonNode actualNode = JsonSchemaUtil.stringToNode(actual);
        JsonValue actualJsonObject = JsonValue.of(actualNode);
        assertTrue(deepEquals(rawSchemaJson, actualJsonObject));
    }

    @Test
    public void toString_ReadOnlyWriteOnly() {
        Schema subject = StringSchema.builder().readOnly(true).writeOnly(false).build();
        JsonObject actual = (JsonObject)JsonValue.of(JsonSchemaUtil.stringToNode(subject.toString()));

        JsonObject expected = ResourceLoader.DEFAULT.readObj("tostring/stringschema-readonly-true-writeonly-false.json");
        assertTrue(deepEquals(actual, expected));
    }

    @Test
    public void requiresString_nullable() {
        Schema subject = StringSchema.builder().requiresString(true).nullable(true).build();
        subject.validate(JsonObject.NULL);
    }

    @Test
    public void getConvertedPattern() {
        StringSchema subject = StringSchema.builder().pattern("my\\\\/[p]a[tt]ern").build();
        assertEquals("my\\\\/[p]a[tt]ern", subject.getRegexpPattern().toString());
        assertEquals("my\\\\/[p]a[tt]ern", subject.getPattern().toString());
    }

    @Test
    public void getConvertedNullPattern() {
        StringSchema subject = StringSchema.builder().build();
        assertNull(subject.getRegexpPattern());
        assertNull(subject.getPattern());
    }

    @Test
    public void regexpFactoryIsUsedByLoader() {
        SchemaLoader loader = SchemaLoader.builder()
                .regexpFactory(new JavaUtilRegexpFactory())
                .schemaJson(ResourceLoader.DEFAULT.readObj("tostring/stringschema.json"))
                .build();

        StringSchema result = (StringSchema) loader.load().build();

        assertEquals(result.getRegexpPattern().getClass().getSimpleName(), "JavaUtilRegexp");
    }

}
