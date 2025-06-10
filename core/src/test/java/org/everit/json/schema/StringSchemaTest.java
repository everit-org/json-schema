package org.everit.json.schema;

import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.everit.json.schema.TestSupport.buildWithLocation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.everit.json.schema.internal.EmailFormatValidator;
import org.everit.json.schema.internal.URIFormatValidator;
import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.regexp.RE2JRegexpFactory;
import org.json.JSONObject;

import com.google.re2j.Pattern;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

public class StringSchemaTest {

    private static Schema loadWithNullableSupport(JSONObject rawSchemaJson) {
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
            fail();
        } catch (ValidationException e) {
            assertEquals(3, e.getCausingExceptions().size());
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

    @Test
    public void issue38Pattern() {
        assertThrows(ValidationException.class, () -> {
            StringSchema.builder().requiresString(true).pattern("\\+?\\d+").build().validate("aaa");
        });
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(StringSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation", "location")
                .withPrefabValues(Pattern.class, Pattern.compile("red"), Pattern.compile("black"))
                .withPrefabValues(FormatValidator.class, new EmailFormatValidator(), new URIFormatValidator())
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        setExamplesAsString(rawSchemaJson);
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toStringWithNullableTrueTest() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.put("nullable", true);
        String actual = loadWithNullableSupport(rawSchemaJson).toString();
        setExamplesAsString(rawSchemaJson);
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toStringWithNullableFalseTest() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.put("nullable", false);
        String actual = loadWithNullableSupport(rawSchemaJson).toString();
        setExamplesAsString(rawSchemaJson);
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toStringNoFormat() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.remove("format");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        setExamplesAsString(rawSchemaJson);
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toStringNoExplicitType() {
        JSONObject rawSchemaJson = ResourceLoader.DEFAULT.readObj("tostring/stringschema.json");
        rawSchemaJson.remove("type");
        String actual = SchemaLoader.load(rawSchemaJson).toString();
        setExamplesAsString(rawSchemaJson);
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void toString_ReadOnlyWriteOnly() {
        Schema subject = StringSchema.builder().readOnly(true).writeOnly(false).build();
        JSONObject actual = new JSONObject(subject.toString());

        JSONObject expected = ResourceLoader.DEFAULT.readObj("tostring/stringschema-readonly-true-writeonly-false.json");

        assertThat(actual, sameJsonAs(expected));
    }

    @Test
    public void requiresString_nullable() {
        Schema subject = StringSchema.builder().requiresString(true).nullable(true).build();
        subject.validate(JSONObject.NULL);
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
                .regexpFactory(new RE2JRegexpFactory())
                .schemaJson(ResourceLoader.DEFAULT.readObj("tostring/stringschema.json"))
                .build();

        StringSchema result = (StringSchema) loader.load().build();

        assertEquals(result.getRegexpPattern().getClass().getSimpleName(), "RE2JRegexp");
    }

    @Test
    public void shouldHaveExamplesValue(){
        SchemaLoader loader = SchemaLoader.builder()
                .regexpFactory(new RE2JRegexpFactory())
                .schemaJson(ResourceLoader.DEFAULT.readObj("tostring/stringschema.json"))
                .build();

        StringSchema result = (StringSchema) loader.load().build();

        assertEquals("[example1, example2]",result.getExamples());
    }

    private void setExamplesAsString(JSONObject rawSchemaJson){
        rawSchemaJson.put("examples","[example1, example2]");
    }

}
