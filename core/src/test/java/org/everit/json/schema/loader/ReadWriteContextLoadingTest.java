package org.everit.json.schema.loader;

import static org.everit.json.schema.TestSupport.loadAsV6;
import static org.everit.json.schema.TestSupport.loadAsV7;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.junit.Test;

public class ReadWriteContextLoadingTest {

    private static ResourceLoader LOADER = ResourceLoader.DEFAULT;

    @Test
    public void testReadOnlyContext() {
        ObjectSchema rootSchema = (ObjectSchema) loadAsV7(LOADER.readObj("read-write-context.json"));
        Schema readOnlyProp = rootSchema.getPropertySchemas().get("readOnlyProp");
        assertTrue(readOnlyProp.isReadOnly());
    }

    @Test
    public void testWriteOnlyContext() {
        ObjectSchema rootSchema = (ObjectSchema) loadAsV7(LOADER.readObj("read-write-context.json"));
        Schema writeOnlyProp = rootSchema.getPropertySchemas().get("writeOnlyProp");
        assertTrue(writeOnlyProp.isWriteOnly());
    }

    @Test
    public void worksOnlyInV7Mode() {
        ObjectSchema rootSchema = (ObjectSchema) loadAsV6(LOADER.readObj("read-write-context.json"));
        Schema readOnlyProp = rootSchema.getPropertySchemas().get("readOnlyProp");
        Schema writeOnlyProp = rootSchema.getPropertySchemas().get("writeOnlyProp");
        assertNull(readOnlyProp.isReadOnly());
        assertNull(writeOnlyProp.isWriteOnly());
    }

}
