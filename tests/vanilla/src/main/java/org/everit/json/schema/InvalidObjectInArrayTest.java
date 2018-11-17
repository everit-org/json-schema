package org.everit.json.schema;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;

public class InvalidObjectInArrayTest {

    private JSONObject readObject(final String fileName) {
        try {
            return new JSONObject(new JSONTokener(IOUtils.toString(getClass()
                    .getResourceAsStream("/org/everit/json/schema/invalidobjectinarray/" + fileName), Charsets.toCharset("UTF-8"))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void test() {
        Schema schema = SchemaLoader.load(readObject("schema.json"));
        Object subject = readObject("subject.json");
        try {
            schema.validate(subject);
            Assert.fail("did not throw exception");
        } catch (ValidationException e) {
            Assert.assertEquals("#/notification/target/apps/0/id", e.getPointerToViolation());
        }
    }

}
