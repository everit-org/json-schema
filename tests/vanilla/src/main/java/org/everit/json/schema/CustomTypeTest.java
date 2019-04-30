package org.everit.json.schema;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;


import static java.util.Objects.requireNonNull;
import static org.everit.json.schema.FormatValidator.NONE;

import java.util.Objects;

import org.everit.json.schema.internal.JSONPrinter;
import org.everit.json.schema.regexp.JavaUtilRegexpFactory;
import org.everit.json.schema.regexp.Regexp;

public class CustomTypeTest {

    @Test
    public void validateCustomType() throws IOException {

        JSONObject jsonSchema = new JSONObject(new JSONTokener(
            IOUtils.toString(
                new InputStreamReader(getClass().getResourceAsStream("/org/everit/json/schema/customType/schema.json")))
        ));

        JSONObject worksJson = new JSONObject(new JSONTokener(
            IOUtils.toString(
                new InputStreamReader(getClass().getResourceAsStream("/org/everit/json/schema/customType/works.json")))
        ));

        JSONObject failsJson = new JSONObject(new JSONTokener(
            IOUtils.toString(
                new InputStreamReader(getClass().getResourceAsStream("/org/everit/json/schema/customType/fails.json")))
        ));

	SchemaLoader.SchemaLoaderBuilder loaderBuilder = SchemaLoader.builder();
	loaderBuilder.addCustomType("customType",CustomTestSchema.class);
	//loaderBuilder.schemaJson(jsonSchema);
	SchemaLoader sLoader = loaderBuilder.build();
	
        Schema schema = sLoader.load(jsonSchema);
        
        schema.validate(worksJson);
        schema.validate(failsJson);
    }

}
