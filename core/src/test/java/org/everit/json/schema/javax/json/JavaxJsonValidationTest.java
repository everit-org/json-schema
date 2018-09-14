package org.everit.json.schema.javax.json;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.everit.json.schema.spi.JsonAdapter;
import org.everit.json.schema.spi.JsonArrayAdapter;
import org.everit.json.schema.spi.JsonObjectAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class JavaxJsonValidationTest {

    private static final Jsr374Adaptation JSR_374_ADAPTATION = new Jsr374Adaptation();
    private static final Jsr353Adaptation JSR_353_ADAPTATION = new Jsr353Adaptation();

    public Object[] javaxJsonAdaptations() {
        return new Object[] { JSR_374_ADAPTATION, JSR_353_ADAPTATION };
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testArrayType(JavaxJsonAdaptation adaptation) {
        assertEquals(JsonArray.class, adaptation.arrayType());
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testObjectType(JavaxJsonAdaptation adaptation) {
        assertEquals(JsonObject.class, adaptation.objectType());
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testIsSupportedType(JavaxJsonAdaptation adaptation) {
        assertTrue(adaptation.isSupportedType(JsonValue.class));
        assertTrue(adaptation.isSupportedType(JsonNumber.class));
        assertTrue(adaptation.isSupportedType(JsonString.class));
        assertTrue(adaptation.isSupportedType(JsonObject.class));
        assertTrue(adaptation.isSupportedType(JsonArray.class));
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testSupportedTypes(JavaxJsonAdaptation adaptation) {
        final List<Class<?>> types = Arrays.asList(adaptation.supportedTypes());
        assertEquals(1, types.size());
        assertTrue(types.contains(JsonValue.class));
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testAdaptIntrinsics(JavaxJsonAdaptation adaptation) {
        assertEquals("value", adaptation.adapt("value"));
        assertEquals(true, adaptation.adapt(true));
        assertEquals(1, adaptation.adapt(1));
        assertNull(adaptation.adapt(null));
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testAdaptAdapter(JavaxJsonAdaptation adaptation) {
        final JsonAdapter adapter = () -> null;
        assertSame(adapter, adaptation.adapt(adapter));
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testAdaptJsonScalars(JavaxJsonAdaptation adaptation) {
        assertNull(adaptation.adapt(JsonValue.NULL));
        assertEquals(true, adaptation.adapt(JsonValue.TRUE));
        assertEquals(false, adaptation.adapt(JsonValue.FALSE));
        assertEquals("value", adaptation.adapt(Json.createValue("value")));
        assertEquals(BigInteger.ONE, adaptation.adapt(Json.createValue(1)));
        assertEquals(BigInteger.ONE, adaptation.adapt(Json.createValue(1L)));
        assertEquals(BigInteger.ONE, adaptation.adapt(Json.createValue(BigInteger.ONE)));
        assertEquals(BigDecimal.valueOf(1.1), adaptation.adapt(Json.createValue(BigDecimal.valueOf(1.1))));
        assertEquals(BigDecimal.valueOf(1.1), adaptation.adapt(Json.createValue(1.1)));
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testAdaptJsonObject(JavaxJsonAdaptation adaptation) {
        final JsonObject object = Json.createObjectBuilder().build();
        final Object result = adaptation.adapt(object);
        assertTrue(result instanceof JsonObjectAdapter);
        assertSame(object, ((JsonObjectAdapter) result).unwrap());
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testAdaptJsonArray(JavaxJsonAdaptation adaptation) {
        final JsonArray object = Json.createArrayBuilder().build();
        final Object result = adaptation.adapt(object);
        assertTrue(result instanceof JsonArrayAdapter);
        assertSame(object, ((JsonArrayAdapter) result).unwrap());
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testInvertIntrinsics(JavaxJsonAdaptation adaptation) {
        assertEquals(JsonValue.NULL, adaptation.invert(null));
        assertEquals(JsonValue.TRUE, adaptation.invert(true));
        assertEquals(JsonValue.FALSE, adaptation.invert(false));
        assertEquals(Json.createValue("value"), adaptation.invert("value"));
        assertEquals(Json.createValue(1), adaptation.invert(1));
        assertEquals(Json.createValue(1L), adaptation.invert(1L));
        assertEquals(Json.createValue(1.0), adaptation.invert(1.0));
        assertEquals(Json.createValue(BigInteger.ONE), adaptation.invert(BigInteger.ONE));
        assertEquals(Json.createValue(BigDecimal.ONE), adaptation.invert(BigDecimal.ONE));
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testInvertObjectAdapter(JavaxJsonAdaptation adaptation) {
        final JsonObject object = Json.createObjectBuilder().build();
        assertSame(object, adaptation.invert(new JavaxJsonObjectAdapter(object)));
    }

    @Test
    @Parameters(method = "javaxJsonAdaptations")
    public void testInvertArrayAdapter(JavaxJsonAdaptation adaptation) {
        final JsonArray array = Json.createArrayBuilder().build();
        assertSame(array, adaptation.invert(new JavaxJsonArrayAdapter(array)));
    }

    @Test
    public void testNewInstanceDefaultClassLoader()  {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        assertTrue(JavaxJsonAdaptation.newInstance() instanceof Jsr374Adaptation);
        Thread.currentThread().setContextClassLoader(tccl);
    }

    @Test
    public void testNewInstanceThreadContextClassLoader()  {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        assertTrue(JavaxJsonAdaptation.newInstance() instanceof Jsr374Adaptation);
        Thread.currentThread().setContextClassLoader(tccl);
    }

    @Test
    public void testNewInstanceWithClassLoader()  {
        assertTrue(JavaxJsonAdaptation.newInstance(getClass().getClassLoader())
                instanceof Jsr374Adaptation);
    }

    @Test
    public void testDetermineProvider() {
        assertEquals(JavaxJsonAdaptation.JSR_374_ADAPTATION,
                JavaxJsonAdaptation.determineProvider("createValue", String.class));
        assertEquals(JavaxJsonAdaptation.JSR_353_ADAPTATION,
                JavaxJsonAdaptation.determineProvider(".noSuchMethod"));
    }

    @Test(expected = RuntimeException.class)
    public void testInstanceWhenCannotInstantiate() {
        JavaxJsonAdaptation.newInstance(getClass().getClassLoader(), ".noSuchProviderName");
    }

}
