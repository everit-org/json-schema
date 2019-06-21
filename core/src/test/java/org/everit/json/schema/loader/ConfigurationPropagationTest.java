package org.everit.json.schema.loader;

import static org.everit.json.schema.JSONMatcher.sameJsonAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;

import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json2.JSONObject;
import org.junit.Test;

public class ConfigurationPropagationTest {

    private static final ResourceLoader LOADER = new ResourceLoader("/org/everit/jsonvalidator/config-propagation/");

    @Test
    public void configurationPropagationTest() throws URISyntaxException {
        SchemaLoader loader = SchemaLoader.builder()
                .schemaClient(SchemaClient.classPathAwareClient())
                .nullableSupport(true)
                .useDefaults(true)
                .registerSchemaByURI(new URI("urn:uuid:85946d9c-b896-496c-a7ac-6835f4b59f63"), LOADER.readObj("schema-by-urn.json"))
                .addFormatValidator(new CustomFormatValidatorTest.EvenCharNumValidator())
                .schemaJson(LOADER.readObj("schema.json"))
                .build();
        Schema actual = loader.load().build();
        JSONObject instance = LOADER.readObj("instance.json");
        try {
            actual.validate(instance);
            fail("did not throw validation exception");
        } catch (ValidationException e) {
            assertThat(e.toJSON(), sameJsonAs(LOADER.readObj("expected-exception.json")));
        }
        assertEquals(42, instance.get("propWithDefault"));
    }

}
