package org.everit.json.schema.event;

import static java.util.Arrays.asList;
import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.everit.json.schema.event.ConditionalSchemaValidationEvent.Keyword.IF;
import static org.junit.Assert.assertThat;

import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ConditionalSchema;
import org.everit.json.schema.FalseSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.TrueSchema;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class EventToStringTest {

    private static final ResourceLoader LOADER = new ResourceLoader("/org/everit/jsonvalidator/event/");

    private static final ReferenceSchema REF_SCHEMA = ReferenceSchema.builder()
            .refValue("#/definitions/Stuff")
            .build();

    private static final StringSchema REFERRED_SCHEMA = StringSchema.builder()
            .minLength(22)
            .build();

    private static final JSONObject INSTANCE = new JSONObject();

    private static final ConditionalSchema CONDITIONAL_SCHEMA = ConditionalSchema.builder()
            .ifSchema(TrueSchema.builder().build())
            .build();
    public static final CombinedSchema COMBINED_SCHEMA = CombinedSchema.builder()
            .criterion(CombinedSchema.ANY_CRITERION)
            .subschemas(asList(
                    TrueSchema.INSTANCE,
                    FalseSchema.INSTANCE
            )).build();

    static {
        INSTANCE.put("hello", new JSONArray("[\"world\"]"));
    }

    private static final SchemaReferencedEvent REF_EVENT = new SchemaReferencedEvent(REF_SCHEMA, INSTANCE, REFERRED_SCHEMA);

    @Test
    public void schemaReferenceEventToStringTest() {
        JSONObject expected = LOADER.readObj("reference-event.json");
        SchemaReferencedEvent subject = REF_EVENT;

        JSONObject actual = new JSONObject(subject.toString());
        assertThat(actual, sameJsonAs(expected));
    }

    @Test
    public void schemaReferenceWithSchemaToString() {
        JSONObject expected = LOADER.readObj("reference-event-with-schema.json");
        SchemaReferencedEvent subject = REF_EVENT;

        JSONObject actual = new JSONObject(subject.toJSON(true, false).toString());
        assertThat(actual, sameJsonAs(expected));
    }

    @Test
    public void schemaReferenceWithInstanceToString() {
        JSONObject expected = LOADER.readObj("reference-event-with-instance.json");
        SchemaReferencedEvent subject = REF_EVENT;

        JSONObject actual = new JSONObject(subject.toJSON(false, true).toString());
        assertThat(actual, sameJsonAs(expected));
    }

    @Test
    public void conditionalSchemaMatchEventToString_ifMatch() {
        JSONObject expected = LOADER.readObj("conditional-match-if.json");
        ConditionalSchemaMatchEvent subject = new ConditionalSchemaMatchEvent(CONDITIONAL_SCHEMA, INSTANCE, IF);

        JSONObject actual = new JSONObject(subject.toString());
        assertThat(actual, sameJsonAs(expected));
    }

    @Test
    public void conditionalSchemaMismatchEvent() {
        JSONObject expected = LOADER.readObj("conditional-mismatch-if.json");
        ValidationException exc = new ValidationException(CONDITIONAL_SCHEMA, "message", "if", "#/schema/location");
        ConditionalSchemaMismatchEvent subject = new ConditionalSchemaMismatchEvent(CONDITIONAL_SCHEMA, INSTANCE, IF, exc);

        JSONObject actual = new JSONObject(subject.toString());
        assertThat(actual, sameJsonAs(expected));
    }

    @Test
    public void combinedSchemaMatchEventToString() {
        JSONObject expected = LOADER.readObj("combined-schema-match.json");
        CombinedSchemaMatchEvent subject = new CombinedSchemaMatchEvent(COMBINED_SCHEMA, TrueSchema.INSTANCE, INSTANCE);

        JSONObject actual = new JSONObject(subject.toString());

        assertThat(actual, sameJsonAs(expected));
    }

    @Test
    public void combinedSchemaMismatchEventToString() {
        JSONObject expected = LOADER.readObj("combined-schema-mismatch.json");
        ValidationException exc = new ValidationException(COMBINED_SCHEMA, "message", "anyOf", "#/schema/location");
        CombinedSchemaMismatchEvent subject = new CombinedSchemaMismatchEvent(COMBINED_SCHEMA, FalseSchema.INSTANCE, INSTANCE, exc);

        JSONObject actual = new JSONObject(subject.toJSON(true, true).toString());

        assertThat(actual, sameJsonAs(expected));
    }

}
