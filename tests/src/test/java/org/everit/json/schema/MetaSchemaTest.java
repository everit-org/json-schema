package org.everit.json.schema;

import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.JsonValue;
import org.everit.json.schema.loader.SchemaLoader;
import org.junit.Test;

public class MetaSchemaTest {

    @Test
    public void validateMetaSchema() {

    	JsonObject jsonSchema = (JsonObject)JsonValue.of(JsonSchemaUtil.streamToNode(
                MetaSchemaTest.class
                        .getResourceAsStream("/org/everit/json/schema/json-schema-draft-04.json")));

    	JsonObject jsonSubject = (JsonObject)JsonValue.of(JsonSchemaUtil.streamToNode(
                MetaSchemaTest.class
                        .getResourceAsStream("/org/everit/json/schema/json-schema-draft-04.json")));

        Schema schema = SchemaLoader.load(jsonSchema);
        schema.validate(jsonSubject);
    }

}
