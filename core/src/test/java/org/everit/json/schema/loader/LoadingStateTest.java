package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class LoadingStateTest {

    @Test
    public void testCreateSchemaException() {
        LoadingState subject = new LoadingState(SchemaLoader.builder().schemaJson(new JSONObject()));
        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#: message", actual.getMessage());
    }
}
