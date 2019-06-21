package org.everit.json.schema;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.loader.SchemaLoader;
import org.json2.JSONObject;
import org.json2.JSONTokener;
import org.junit.Test;

public class MetaSchemaTest {

    @Test
    public void validateMetaSchema() throws IOException {

        JSONObject jsonSchema = new JSONObject(new JSONTokener(
                IOUtils.toString(
                        new InputStreamReader(getClass().getResourceAsStream("/org/everit/json/schema/json-schema-draft-04.json")))
        ));

        Schema schema = SchemaLoader.load(jsonSchema);
        schema.validate(jsonSchema);
    }

}
