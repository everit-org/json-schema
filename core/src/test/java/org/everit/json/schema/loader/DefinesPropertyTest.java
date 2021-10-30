package org.everit.json.schema.loader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ResourceLoader;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class DefinesPropertyTest {

    private static JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("testschemas.json");

    private JSONObject get(final String schemaName) {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    private static final ArraySchema TUPLE_SCHEMA = ArraySchema.builder()
            .minItems(2).maxItems(6)
            .addItemSchema(ObjectSchema.builder()
                    .addPropertySchema("sub1prop", BooleanSchema.INSTANCE)
                    .addPropertySchema("commonprop", BooleanSchema.INSTANCE)
                    .build())
            .addItemSchema(ObjectSchema.builder()
                    .addPropertySchema("sub2prop", BooleanSchema.INSTANCE)
                    .addPropertySchema("commonprop", BooleanSchema.INSTANCE)
                    .build())
            .schemaOfAdditionalItems(ObjectSchema.builder()
                    .addPropertySchema("sub3prop", BooleanSchema.INSTANCE)
                    .addPropertySchema("commonprop", BooleanSchema.INSTANCE)
                    .build())
            .build();

    private static final ArraySchema TUPLE_WITHOUT_ADDITIONAL = ArraySchema.builder()
            .addItemSchema(BooleanSchema.INSTANCE)
            .addItemSchema(NullSchema.INSTANCE)
            .additionalItems(false)
            .maxItems(10)
            .build();

    private static final ArraySchema ARRAY_SCHEMA = ArraySchema.builder()
            .minItems(2).maxItems(6)
            .allItemSchema(ObjectSchema.builder()
                    .addPropertySchema("prop", BooleanSchema.INSTANCE)
                    .build())
            .build();

    @Test
    public void objectSchemaHasField() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("pointerResolution"));
        assertTrue(actual.definesProperty("#/rectangle"));
        assertTrue(actual.definesProperty("#/rectangle/a"));
        assertTrue(actual.definesProperty("#/rectangle/b"));

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

        assertTrue(recursiveSchema.definesProperty("#/prop"));
        assertTrue(recursiveSchema.definesProperty("#/prop/subprop"));
        assertTrue(recursiveSchema.definesProperty("#/prop/subprop/subprop"));
        assertTrue(recursiveSchema.definesProperty("#/prop/subprop/subprop/subprop"));
    }

    @Test
    public void patternPropertiesHasField() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("patternProperties"));
        assertTrue(actual.definesProperty("#/a"));
        assertTrue(actual.definesProperty("#/aa"));
        assertTrue(actual.definesProperty("#/aaa"));
        assertTrue(actual.definesProperty("#/aaaa"));
        assertTrue(actual.definesProperty("#/aaaaa"));
        
        assertFalse(actual.definesProperty("b"));
    }

    @Test
    public void objectWithSchemaDep() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithSchemaDep"));
        assertTrue(actual.definesProperty("#/a"));
        assertTrue(actual.definesProperty("#/b"));

        assertFalse(actual.definesProperty("#/c"));
    }

    @Test
    public void objectWithSchemaRectangleDep() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithSchemaRectangleDep"));
        assertTrue(actual.definesProperty("#/d"));
        assertTrue(actual.definesProperty("#/rectangle/a"));
        assertTrue(actual.definesProperty("#/rectangle/b"));

        assertFalse(actual.definesProperty("#/c"));
        assertFalse(actual.definesProperty("#/d/c"));
        assertFalse(actual.definesProperty("#/rectangle/c"));
    }

    @Test
    public void objectEscape() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectEscape"));
        assertTrue(actual.definesProperty("#/a~0b"));
        assertTrue(actual.definesProperty("#/a~0b/c~1d"));

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
        assertTrue(actual.definesProperty("#/1stLevel/2ndLevel/3rdLev"));
        // Assert.assertTrue(actual.definesProperty("#/1stLevel/2ndLevel/3rdLevel/4thLevel"));
    }

    @Test
    void tupleSchema_definesIndex() {
        assertTrue(TUPLE_SCHEMA.definesProperty("#/0"));
    }

    @Test
    void tupleSchema_definesAdditionalIndex() {
        assertTrue(TUPLE_SCHEMA.definesProperty("#/2"));
    }

    @Test
    void tupleSchema_doesNotDefine_IndexGreaterThan_maxItems() {
        assertFalse(TUPLE_SCHEMA.definesProperty("#/6"));
    }

    @Test
    void tupleSchema_defines_index_tupleSubschema() {
        assertTrue(TUPLE_SCHEMA.definesProperty("#/0/sub1prop"));
        assertTrue(TUPLE_SCHEMA.definesProperty("#/1/sub2prop"));
    }

    @Test
    void tupleSchema_defines_index_doesNotDefine_subschemaProp() {
        assertFalse(TUPLE_SCHEMA.definesProperty("#/0/nonexistent"));
        assertFalse(TUPLE_SCHEMA.definesProperty("#/5/nonexistent"));
    }

    @Test
    void tupleSchema_doesNotDefine_negativeIndex() {
        assertFalse(TUPLE_SCHEMA.definesProperty("#/-1"));
    }

    @Test
    void tupleSchema_additionalPropsFalse_doesNotDefine()  {
        assertFalse(TUPLE_WITHOUT_ADDITIONAL.definesProperty("#/8"));
    }

    @Test
    void tupleSchema_additionalPropsFalse_definesIndex_inBound() {
        assertFalse(TUPLE_WITHOUT_ADDITIONAL.definesProperty("#/0"));
    }

    @Test
    void arraySchema_definesIndex_inBound() {
        assertTrue(ARRAY_SCHEMA.definesProperty("#/5/prop"));
    }

    @Test
    void arraySchema_doesNotDefineIndex_greaterThan_maxLength() {
        assertFalse(ARRAY_SCHEMA.definesProperty("#/10"));
    }

    @Test
    void arraySchema_definesIndex_noRemaining(){
        assertTrue(ARRAY_SCHEMA.definesProperty("#/5"));
    }

    @Test
    void arraySchema_nonNumericIndex(){
        assertFalse(ARRAY_SCHEMA.definesProperty("#/prop"));
    }

    @Test
    void arraySchema_floatIndex() {
        assertFalse(ARRAY_SCHEMA.definesProperty("#/12.34"));
    }

    @Test
    void arraySchema_all_definesProperty() {
        assertTrue(ARRAY_SCHEMA.definesProperty("#/all"));
        assertTrue(ARRAY_SCHEMA.definesProperty("#/all/prop"));
        assertFalse(ARRAY_SCHEMA.definesProperty("#/all/nonexistent"));
    }

    @Test
    void arraySchema_any_definesProperty() {
        assertTrue(ARRAY_SCHEMA.definesProperty("#/any"));
        assertTrue(ARRAY_SCHEMA.definesProperty("#/any/prop"));
    }

    @Test
    void tupleSchema_all() {
        assertTrue(TUPLE_SCHEMA.definesProperty("#/all"));
        assertFalse(TUPLE_SCHEMA.definesProperty("#/all/sub1prop"));
        assertTrue(TUPLE_SCHEMA.definesProperty("#/all/commonprop"));
    }

    @Test
    void tupleSchema_any() {
        assertTrue(TUPLE_SCHEMA.definesProperty("#/any/sub1prop"));
        assertTrue(TUPLE_SCHEMA.definesProperty("#/any/sub2prop"));
        assertTrue(TUPLE_SCHEMA.definesProperty("#/any/sub3prop"));
        assertFalse(TUPLE_SCHEMA.definesProperty("#/any/nonexistent"));
    }
}
