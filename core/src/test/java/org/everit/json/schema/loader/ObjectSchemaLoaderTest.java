package org.everit.json.schema.loader;

import org.everit.json.schema.*;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author erosb
 */
public class ObjectSchemaLoaderTest {

    private static JSONObject ALL_SCHEMAS = ResourceLoader.DEFAULT.readObj("objecttestschemas.json");

    private static JSONObject get(final String schemaName) {
        return ALL_SCHEMAS.getJSONObject(schemaName);
    }

    @Rule
    public ExpectedException expExc = ExpectedException.none();

    @Test
    public void objectSchema() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectSchema"));
        assertNotNull(actual);
        Map<String, Schema> propertySchemas = actual.getPropertySchemas();
        assertEquals(2, propertySchemas.size());
        assertEquals(BooleanSchema.INSTANCE, propertySchemas.get("boolProp"));
        Assert.assertFalse(actual.permitsAdditionalProperties());
        assertEquals(2, actual.getRequiredProperties().size());
        assertEquals(2, actual.getMinProperties().intValue());
        assertEquals(3, actual.getMaxProperties().intValue());
    }

    @Test(expected = SchemaException.class)
    public void objectInvalidAdditionalProperties() {
        SchemaLoader.load(get("objectInvalidAdditionalProperties"));
    }

    @Test
    public void objectWithAdditionalPropSchema() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithAdditionalPropSchema"));
        assertEquals(BooleanSchema.INSTANCE, actual.getSchemaOfAdditionalProperties());
    }

    @Test
    public void objectWithPropDep() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithPropDep"));
        assertEquals(1, actual.getPropertyDependencies().get("isIndividual").size());
    }

    @Test
    public void objectWithSchemaDep() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithSchemaDep"));
        assertEquals(1, actual.getSchemaDependencies().size());
    }

    @Test
    public void patternProperties() {
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("patternProperties"));
        assertNotNull(actual);
        assertEquals(2, actual.getPatternProperties().size());
    }

    @Test(expected = SchemaException.class)
    public void invalidDependency() {
        SchemaLoader.load(get("invalidDependency"));
    }

    @Test
    public void emptyDependencyList() {
        SchemaLoader.load(get("emptyDependencyList"));
    }

    @Test
    public void invalidRequired() {
        expExc.expect(SchemaException.class);
        expExc.expectMessage("#/required/1: expected type: String, found: JsonArray");
        SchemaLoader.load(get("invalidRequired"));
    }

    @Test
    public void booleanDependency() {
        ObjectSchema actual = (ObjectSchema) TestSupport.loadAsV6(get("booleanDependencies"));
        assertEquals(actual.getSchemaDependencies().get("foo"), TrueSchema.builder().build());
    }

    @Test
    public void properyNamesV6() {
        ObjectSchema actual = (ObjectSchema) TestSupport.loadAsV6(get("propertyNames"));
        assertNotNull(actual.getPropertyNameSchema());
    }

    @Test
    public void properyNamesV4() {
        JSONObject rawSchema = get("propertyNames");
        rawSchema.put("type", "object");
        ObjectSchema actual = (ObjectSchema) SchemaLoader.load(rawSchema);
        assertNull(actual.getPropertyNameSchema());
    }

}
