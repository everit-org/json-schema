package org.everit.json.schema.loader;

import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_4;
import static org.everit.json.schema.loader.SpecificationVersion.DRAFT_6;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.SchemaException;
import org.json.JSONObject;
import org.junit.Test;

public class SpecVersionDeductionTest {

    private static JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("testschemas.json");

    private static JSONObject get(String schemaName) {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    private void assertSchemaException(SchemaLoader.SchemaLoaderBuilder loaderBuilder) {
        try {
            loaderBuilder.build();
            fail("did not throw exception");
        } catch (SchemaException e) {
            assertEquals("#", e.getSchemaLocation());
        }
    }

    private void assertSpecVersion(SpecificationVersion version, SchemaLoader.SchemaLoaderBuilder loaderBuilder) {
        assertSame(version, loaderBuilder.build().specVersion());
    }

    @Test
    public void unknownMetaSchema_implicitDefault() {
        assertSchemaException(SchemaLoader.builder().schemaJson(get("unknownMetaSchema")));
    }

    @Test
    public void knownMetaSchema_implicitDefault() {
        assertSpecVersion(DRAFT_6, SchemaLoader.builder().schemaJson(get("explicitSchemaVersion")));
    }

    @Test
    public void missingMetaSchema_implicitDefault() {
        assertSpecVersion(DRAFT_4, SchemaLoader.builder().schemaJson(get("constSchema")));
    }

    @Test
    public void unknownMetaSchema_explicitDefault() {
        assertSpecVersion(DRAFT_6, SchemaLoader.builder()
                .draftV6Support()
                .schemaJson(get("unknownMetaSchema")));
    }

    @Test
    public void knownMetaSchema_explicitDefault() {
        assertSpecVersion(DRAFT_6, SchemaLoader.builder()
                .draftV7Support()
                .schemaJson(get("explicitSchemaVersion")));
    }

    @Test
    public void missingMetaSchema_explicitDefault() {
        assertSpecVersion(DRAFT_6, SchemaLoader.builder()
                .draftV6Support()
                .schemaJson(get("constSchema")));
    }

}
