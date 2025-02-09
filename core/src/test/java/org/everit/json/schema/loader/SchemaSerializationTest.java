package org.everit.json.schema.loader;

import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchemaSerializationTest {

    private static JSONObject SCHEMA_JSON = ResourceLoader.DEFAULT.readObj("testschemas.json").getJSONObject("pointerResolution");

    @Test
    void testBackAndForthSerializationWithPointer()
            throws Exception {
        Schema loaded = SchemaLoader.load(SCHEMA_JSON);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(loaded);

        oos.flush();
        baos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Schema readBack = (Schema) ois.readObject();
        assertEquals(loaded, readBack);
    }
}
