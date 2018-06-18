package org.everit.json.schema.loader;

import static org.junit.Assert.assertFalse;

import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class DefinesPropertyTest {

    private static JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("testschemas.json");

    private JSONObject get(final String schemaName) {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    @Test
    public void objectSchemaHasField() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("pointerResolution"));
        Assert.assertTrue(actual.definesProperty("#/rectangle"));
        Assert.assertTrue(actual.definesProperty("#/rectangle/a"));
        Assert.assertTrue(actual.definesProperty("#/rectangle/b"));

        assertFalse(actual.definesProperty("#/rectangle/c"));
        assertFalse(actual.definesProperty("#/rectangle/"));
        assertFalse(actual.definesProperty("#/"));
        assertFalse(actual.definesProperty("#/a"));
        assertFalse(actual.definesProperty("#"));
        assertFalse(actual.definesProperty("#/rectangle/a/d"));
    }

    @Test
    public void recursiveSchemaHasField() {
        Schema recursiveSchema = SchemaLoader.load(get("recursiveSchema"));

        Assert.assertTrue(recursiveSchema.definesProperty("#/prop"));
        Assert.assertTrue(recursiveSchema.definesProperty("#/prop/subprop"));
        Assert.assertTrue(recursiveSchema.definesProperty("#/prop/subprop/subprop"));
        Assert.assertTrue(recursiveSchema.definesProperty("#/prop/subprop/subprop/subprop"));
    }

    @Test
    public void patternPropertiesHasField() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("patternProperties"));
        Assert.assertTrue(actual.definesProperty("#/a"));
        Assert.assertTrue(actual.definesProperty("#/aa"));
        Assert.assertTrue(actual.definesProperty("#/aaa"));
        Assert.assertTrue(actual.definesProperty("#/aaaa"));
        Assert.assertTrue(actual.definesProperty("#/aaaaa"));
        
        assertFalse(actual.definesProperty("b"));
    }

    @Test
    public void objectWithSchemaDep() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithSchemaDep"));
        Assert.assertTrue(actual.definesProperty("#/a"));
        Assert.assertTrue(actual.definesProperty("#/b"));

        assertFalse(actual.definesProperty("#/c"));
    }

    @Test
    public void objectWithSchemaRectangleDep() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithSchemaRectangleDep"));
        Assert.assertTrue(actual.definesProperty("#/d"));
        Assert.assertTrue(actual.definesProperty("#/rectangle/a"));
        Assert.assertTrue(actual.definesProperty("#/rectangle/b"));

        assertFalse(actual.definesProperty("#/c"));
        assertFalse(actual.definesProperty("#/d/c"));
        assertFalse(actual.definesProperty("#/rectangle/c"));
    }

    @Test
    public void objectEscape() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectEscape"));
        Assert.assertTrue(actual.definesProperty("#/a~0b"));
        Assert.assertTrue(actual.definesProperty("#/a~0b/c~1d"));

        assertFalse(actual.definesProperty("#/a~0b/c/d"));
    }

    @Test
    public void definesPropertyIfSubschemaMatchCountIsAcceptedByCriterion() {
        CombinedSchema subject = CombinedSchema.builder()
                .subschema(ObjectSchema.builder().addPropertySchema("a", BooleanSchema.INSTANCE).build())
                .subschema(ObjectSchema.builder().addPropertySchema("b", BooleanSchema.INSTANCE).build())
                .criterion((subschemaCount, matchingSubschemaCount) -> {
                    if (matchingSubschemaCount == 1 && subschemaCount == 2) {
                        // dummy exception
                        throw new ValidationException(Object.class, new Object());
                    }
                })
                .build();
        assertFalse(subject.definesProperty("a"));
    }

    @Test
    public void testOfTest() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("patternPropsAndSchemaDeps"));
        JSONObject input = ResourceLoader.DEFAULT
                .readObj("objecttestcases.json")
                .getJSONObject("validOfPatternPropsAndSchemaDeps");
        actual.validate(input);
    }

    @Test
    public void patternPropsAndSchemaDefs() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("patternPropsAndSchemaDeps"));
        // Assert.assertTrue(actual.definesProperty("#/1stLevel"));
        // Assert.assertTrue(actual.definesProperty("#/1stLevel/2ndLevel"));
        Assert.assertTrue(actual.definesProperty("#/1stLevel/2ndLevel/3rdLev"));
        // Assert.assertTrue(actual.definesProperty("#/1stLevel/2ndLevel/3rdLevel/4thLevel"));
    }

}
