package org.everit.json.schema.loader;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.SchemaLocation;
import org.everit.json.schema.loader.internal.DefaultProviderValidators;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.junit.jupiter.api.Test;

public class SubschemaRegistryTest {

    static final LoaderConfig CONFIG = new LoaderConfig(new DefaultSchemaClient(), new DefaultProviderValidators(), SpecificationVersion.DRAFT_6, false);

    @Test
    public void emptySchemaContainsNoElems() {
        JsonValue obj = JsonValue.of(emptyMap());
        new LoadingState(CONFIG, emptyMap(), obj, obj, null, SchemaLocation.empty());

        SubschemaRegistry registry = new SubschemaRegistry(obj);

        assertEquals(0, registry.storage.size());
    }

    @Test
    public void topLevelIdIsRecognized() {
        JsonValue obj = JsonValue.of(ResourceLoader.DEFAULT.readObj("testschemas.json").getJSONObject("schemaWithIdV6"));
        new LoadingState(CONFIG, emptyMap(), obj, obj, null, SchemaLocation.empty());

        SubschemaRegistry registry = new SubschemaRegistry(obj);

        JsonObject actual = registry.getById("http://example.org/schema/");
        assertSame(obj, actual);
    }

    @Test
    public void childInObjById_isRecognized() {
        JsonValue obj = JsonValue.of(ResourceLoader.DEFAULT.readObj("ref-lookup-tests.json"));
        new LoadingState(CONFIG, emptyMap(), obj, obj, null, SchemaLocation.empty());

        SubschemaRegistry registry = new SubschemaRegistry(obj);

        JsonObject actual = registry.getById("has-id");
        JsonValue expected = obj.requireObject().require("definitions").requireObject().require("HasId");
        assertEquals(expected.unwrap(), actual.unwrap());
    }

    @Test
    public void childInArrayById_isRecognized() {
        JsonValue obj = JsonValue.of(ResourceLoader.DEFAULT.readObj("ref-lookup-tests.json"));
        new LoadingState(CONFIG, emptyMap(), obj, obj, null, SchemaLocation.empty());

        SubschemaRegistry registry = new SubschemaRegistry(obj);

        JsonObject actual = registry.getById("all-of-part-0");
        JsonValue expected = obj.requireObject().require("definitions").requireObject().require("someAllOf")
            .requireObject().require("allOf").requireArray().at(0);
        assertEquals(expected.unwrap(), actual.unwrap());
    }


}
