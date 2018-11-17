package org.everit.json.schema;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

public class EmptyObjectTest {

    @Test
    public void validateEmptyObject() throws IOException {
        JSONObject jsonSchema = new JSONObject(new JSONTokener(IOUtils.toString(
                new InputStreamReader(getClass().getResourceAsStream("/org/everit/json/schema/json-schema-draft-04.json")))));

        JSONObject jsonSubject = new JSONObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {}\n" +
                "}");

        Schema schema = SchemaLoader.load(jsonSchema);
        schema.validate(jsonSubject);
    }

}
