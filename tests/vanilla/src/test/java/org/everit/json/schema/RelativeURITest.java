package org.everit.json.schema;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

public class RelativeURITest {

    @Test
    public void test() throws Exception {
        JettyWrapper jetty = new JettyWrapper("/org/everit/json/schema/relative-uri/");
        jetty.start();
        try {
            SchemaLoader.builder()
                    .resolutionScope("http://localhost:1234/schema/")
                    .schemaJson(
                            new JSONObject(new JSONTokener(getClass().getResourceAsStream(
                                    "/org/everit/json/schema/relative-uri/schema/main.json"))))
                    .build().load().build();
        } finally {
            jetty.stop();
        }
    }

}
