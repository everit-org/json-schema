package org.everit.json.schema;

import org.everit.json.schema.loader.JsonObject;
import org.everit.json.schema.loader.JsonValue;
import org.everit.json.schema.loader.SchemaLoader;
import org.junit.Test;

public class AllofTest {

    @Test
    public void validateMetaSchema() {

        JsonObject jsonSchema = (JsonObject)JsonValue.of(JsonSchemaUtil.streamToNode(
        		AllofTest.class
                        .getResourceAsStream("/org/everit/json/schema/wm/allof/allOfSchema.json")));

        JsonObject jsonSubject = (JsonObject)JsonValue.of(JsonSchemaUtil.streamToNode(
        		AllofTest.class
                        .getResourceAsStream("/org/everit/json/schema/wm/allof/allOfSchema_subject.json")));

        Schema schema = SchemaLoader.load(jsonSchema);
        schema.validate(jsonSubject);
    }

}
