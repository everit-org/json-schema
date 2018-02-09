package org.everit.json.schema.loader;

import static org.junit.Assert.assertTrue;

import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.junit.Test;

public class ReadWriteContextLoadingTest {

    private static ResourceLoader LOADER = ResourceLoader.DEFAULT;

    @Test
    public void testReadOnlyContext() {
        ObjectSchema rootSchema = (ObjectSchema) SchemaLoader.load(LOADER.readObj("read-write-context.json"));
        Schema readOnlyProp = rootSchema.getPropertySchemas().get("readOnlyProp");
        assertTrue(readOnlyProp.isReadOnly());
    }

    @Test
    public void testWriteOnlyContext() {
        ObjectSchema rootSchema = (ObjectSchema) SchemaLoader.load(LOADER.readObj("read-write-context.json"));
        Schema readOnlyProp = rootSchema.getPropertySchemas().get("writeOnlyProp");
        assertTrue(readOnlyProp.isWriteOnly());
    }

}
