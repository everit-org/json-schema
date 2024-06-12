package org.everit.json.schema;

import static org.everit.json.schema.FalseSchema.INSTANCE;
import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;

import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ToStringTest {

    static class CustomSchema extends Schema {

        static class Builder extends Schema.Builder<CustomSchema, Builder> {

            @Override
            protected Builder getBuilder()
            {
                return this;
            }

            @Override public CustomSchema build() {
                return new CustomSchema(this);
            }
        }

        /**
         * Constructor.
         *
         * @param builder
         *         the builder containing the optional title, description and id attributes of the schema
         */
        protected CustomSchema(Schema.Builder<?, ?> builder) {
            super(builder);
        }

        @Override void accept(Visitor visitor) {
            visitor.visitSchema(this);
        }

        @Override void describePropertiesTo(JSONPrinter writer) {
            writer.key("custom").value("schema");
        }

    }

    private static final ResourceLoader LOADER = new ResourceLoader("/org/everit/jsonvalidator/tostring/");

    @Test
    public void testCustomSchemaWithDescribePropertiesTo() {
        String actual = new CustomSchema(new CustomSchema.Builder().description("descr-custom")).toString();
        assertThat(new JSONObject(actual), sameJsonAs(LOADER.readObj("custom-schema.json")));
    }

    @Test
    public void testBooleanSchema() {
        BooleanSchema subject = BooleanSchema.builder()
                .id("bool-id")
                .title("bool-title")
                .description("bool-description")
                .unprocessedProperties(ImmutableMap.of("$schema", "https://json-schema.org/draft-07/schema"))
                .build();
        assertThat(new JSONObject(subject.toString()), sameJsonAs(LOADER.readObj("boolean-schema.json")));
    }

    @Test
    public void testArraySchema() {
        ArraySchema subject = ArraySchema.builder()
                .uniqueItems(true)
                .minItems(5).maxItems(10)
                .allItemSchema(BooleanSchema.INSTANCE).build();
        String actual = subject.toString();
        assertThat(new JSONObject(actual), sameJsonAs(LOADER.readObj("arrayschema-list.json")));
    }

    @Test
    @Disabled("throws JSONException - bug in JSONWriter")
    public void testFalseSchema() {
        StringWriter w = new StringWriter();
        JSONPrinter writer = new JSONPrinter(w);
        new ToStringVisitor(writer).visit(INSTANCE);
        String actual = w.getBuffer().toString();
        assertEquals("false", actual);
    }

    @Test
    public void idKeywordForDraftV6() {
        Schema subject = ObjectSchema.builder()
                .addPropertySchema("a", ObjectSchema.builder()
                        .addPropertySchema("b", EmptySchema.builder()
                                .description("$schema override should be ignored")
                                .unprocessedProperties(ImmutableMap.of("$schema", "http://json-schema.org/draft-04/schema"))
                                .id("prop-a-b")
                                .build())
                        .description("$id in subschema should work")
                        .id("prop-a")
                        .build())
                .unprocessedProperties(ImmutableMap.of("$schema", "http://json-schema.org/draft-06/schema"))
                .id("root-schema")
                .build();
        String actual = subject.toString();
        JSONObject rawSchemaJson = LOADER.readObj("draft6-schema-id.json");
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void nonStringSchemaVersionIsIgnored() {
        Schema subject = EmptySchema.builder()
                .unprocessedProperties(ImmutableMap.of("$schema", 42))
                .id("my-id")
                .build();
        String actual = subject.toString();
        JSONObject rawSchemaJson = LOADER.readObj("nonstring-schema-keyword.json");
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

    @Test
    public void unrecognizedSchemaVersionIsIgnored() {
        Schema subject = EmptySchema.builder()
                .unprocessedProperties(ImmutableMap.of("$schema", "http://example.org/nonexistent.json"))
                .id("my-id")
                .build();
        String actual = subject.toString();
        JSONObject rawSchemaJson = LOADER.readObj("unrecognized-schema-keyword.json");
        assertThat(new JSONObject(actual), sameJsonAs(rawSchemaJson));
    }

}
