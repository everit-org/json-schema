package org.everit.json.schema;

import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.json.JSONPointerException;
import org.json.JSONTokener;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is ported from https://raw.githubusercontent.com/stleary/JSON-Java-unit-test/
 */
public class JSONPointerTest {

    private static final JSONObject document;

    static {
        @SuppressWarnings("resource")
        InputStream resourceAsStream = JSONPointerTest.class.getClassLoader().getResourceAsStream("jsonpointer-testdoc.json");
        if (resourceAsStream == null) {
            throw new ExceptionInInitializerError("Unable to locate test file. Please check your development environment configuration");
        }
        document = new JSONObject(new JSONTokener(resourceAsStream));
    }

    private Object query(String pointer) {
        return new JSONPointer(pointer).queryFrom(document);
    }

    @Test
    public void emptyPointer() {
        assertSame(document, query(""));
    }

    @Test
    public void nullPointer() {
        assertThrows(NullPointerException.class, () -> {
            new JSONPointer((String) null);
        });
    }

    @Test
    public void objectPropertyQuery() {
        assertSame(document.get("foo"), query("/foo"));
    }

    @Test
    public void arrayIndexQuery() {
        assertSame(document.getJSONArray("foo").get(0), query("/foo/0"));
    }

    @Test
    public void stringPropOfArrayFailure() {
        assertThrows(JSONPointerException.class, () -> {
            query("/foo/bar");
        });
    }

    @Test
    public void queryByEmptyKey() {
        assertSame(document.get(""), query("/"));
    }

    @Test @Disabled
    public void queryByEmptyKeySubObject() {
        assertSame(document.getJSONObject("obj").getJSONObject(""), query("/obj/"));
    }

    @Test @Disabled
    public void queryByEmptyKeySubObjectSubOject() {
        assertSame(
                document.getJSONObject("obj").getJSONObject("").get(""),
                query("/obj//")
        );
    }

    @Test
    public void queryByEmptyKeySubObjectValue() {
        assertSame(
                document.getJSONObject("obj").getJSONObject("").get("subKey"),
                query("/obj//subKey")
        );
    }

    @Test
    public void slashEscaping() {
        assertSame(document.get("a/b"), query("/a~1b"));
    }

    @Test
    public void tildeEscaping() {
        assertSame(document.get("m~n"), query("/m~0n"));
    }

    @Test
    public void backslashEscaping() {
        assertSame(document.get("i\\j"), query("/i\\j"));
    }

    @Test
    public void quotationEscaping() {
        assertSame(document.get("k\"l"), query("/k\"l"));
    }

    @Test
    public void whitespaceKey() {
        assertSame(document.get(" "), query("/ "));
    }

    @Test
    public void uriFragmentNotation() {
        assertSame(document.get("foo"), query("#/foo"));
    }

    @Test
    public void uriFragmentNotationRoot() {
        assertSame(document, query("#"));
    }

    @Test
    public void uriFragmentPercentHandling() {
        assertSame(document.get("c%d"), query("#/c%25d"));
        assertSame(document.get("e^f"), query("#/e%5Ef"));
        assertSame(document.get("g|h"), query("#/g%7Ch"));
        assertSame(document.get("m~n"), query("#/m~0n"));
    }

    @Test
    public void syntaxError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new JSONPointer("key");
        });
    }

    @Test
    public void arrayIndexFailure() {
        assertThrows(JSONPointerException.class, () -> {
            query("/foo/2");
        });
    }

    @Test
    public void primitiveFailure() {
        assertThrows(JSONPointerException.class, () -> {
            query("/obj/key/failure");
        });
    }

    @Test
    public void builderTest() {
        JSONPointer pointer = JSONPointer.builder()
                .append("obj")
                .append("other~key").append("another/key")
                .append(0)
                .build();
        assertEquals("val", pointer.queryFrom(document));
    }

    @Test
    public void nullToken() {
        assertThrows(NullPointerException.class, () -> {
            JSONPointer.builder().append(null);
        });
    }

    @Test
    public void toStringEscaping() {
        JSONPointer pointer = JSONPointer.builder()
                .append("obj")
                .append("other~key").append("another/key")
                .append("\"")
                .append(0)
                .build();
        assertEquals("/obj/other~0key/another~1key/\"/0", pointer.toString());
    }

    @Test
    public void emptyPointerToString() {
        assertEquals("", new JSONPointer("").toString());
    }

    @Test
    public void toURIFragment() {
        assertEquals("#/c%25d", new JSONPointer("/c%d").toURIFragment());
        assertEquals("#/e%5Ef", new JSONPointer("/e^f").toURIFragment());
        assertEquals("#/g%7Ch", new JSONPointer("/g|h").toURIFragment());
        assertEquals("#/m%7En", new JSONPointer("/m~n").toURIFragment());
    }

    @Test
    public void tokenListIsCopiedInConstructor() {
        JSONPointer.Builder b = JSONPointer.builder().append("key1");
        JSONPointer jp1 = b.build();
        b.append("key2");
        JSONPointer jp2 = b.build();
        if (jp1.toString().equals(jp2.toString())) {
            fail("Oops, my pointers are sharing a backing array");
        }
    }

    /**
     * Coverage for JSONObject query(String)
     */
    @Test
    public void queryFromJSONObject() {
        String str = "{" +
                "\"stringKey\":\"hello world!\"," +
                "\"arrayKey\":[0,1,2]," +
                "\"objectKey\": {" +
                "\"a\":\"aVal\"," +
                "\"b\":\"bVal\"" +
                "}" +
                "}";
        JSONObject jsonObject = new JSONObject(str);
        Object obj = jsonObject.query("/stringKey");
        assertTrue("hello world!".equals(obj), "Expected 'hello world!'");
        obj = jsonObject.query("/arrayKey/1");
        assertTrue(Integer.valueOf(1).equals(obj), "Expected 1");
        obj = jsonObject.query("/objectKey/b");
        assertTrue("bVal".equals(obj), "Expected bVal");
        try {
            obj = jsonObject.query("/a/b/c");
            assertTrue(false,"Expected JSONPointerException");
        } catch (JSONPointerException e) {
            assertTrue( "value [null] is not an array or object therefore its key b cannot be resolved".
                            equals(e.getMessage()), "Expected bad key/value exception");
        }
    }

    /**
     * Coverage for JSONObject query(JSONPointer)
     */
    @Test
    public void queryFromJSONObjectUsingPointer() {
        String str = "{" +
                "\"stringKey\":\"hello world!\"," +
                "\"arrayKey\":[0,1,2]," +
                "\"objectKey\": {" +
                "\"a\":\"aVal\"," +
                "\"b\":\"bVal\"" +
                "}" +
                "}";
        JSONObject jsonObject = new JSONObject(str);
        Object obj = jsonObject.query(new JSONPointer("/stringKey"));
        assertTrue("hello world!".equals(obj), "Expected 'hello world!'");
        obj = jsonObject.query(new JSONPointer("/arrayKey/1"));
        assertTrue(Integer.valueOf(1).equals(obj), "Expected 1");
        obj = jsonObject.query(new JSONPointer("/objectKey/b"));
        assertTrue("bVal".equals(obj), "Expected bVal");
        try {
            obj = jsonObject.query(new JSONPointer("/a/b/c"));
            assertTrue(false, "Expected JSONPointerException");
        } catch (JSONPointerException e) {
            assertTrue("value [null] is not an array or object therefore its key b cannot be resolved".
                            equals(e.getMessage()), "Expected bad key/value exception");
        }
    }

    /**
     * Coverage for JSONObject optQuery(JSONPointer)
     */
    @Test
    public void optQueryFromJSONObjectUsingPointer() {
        String str = "{" +
                "\"stringKey\":\"hello world!\"," +
                "\"arrayKey\":[0,1,2]," +
                "\"objectKey\": {" +
                "\"a\":\"aVal\"," +
                "\"b\":\"bVal\"" +
                "}" +
                "}";
        JSONObject jsonObject = new JSONObject(str);
        Object obj = jsonObject.optQuery(new JSONPointer("/stringKey"));
        assertTrue("hello world!".equals(obj), "Expected 'hello world!'");
        obj = jsonObject.optQuery(new JSONPointer("/arrayKey/1"));
        assertTrue(Integer.valueOf(1).equals(obj), "Expected 1");
        obj = jsonObject.optQuery(new JSONPointer("/objectKey/b"));
        assertTrue("bVal".equals(obj), "Expected bVal");
        obj = jsonObject.optQuery(new JSONPointer("/a/b/c"));
        assertTrue(obj == null, "Expected null");
    }

    /**
     * Coverage for JSONArray query(String)
     */
    @Test
    public void queryFromJSONArray() {
        String str = "[" +
                "\"hello world!\"," +
                "[0,1,2]," +
                "{" +
                "\"a\":\"aVal\"," +
                "\"b\":\"bVal\"" +
                "}" +
                "]";
        JSONArray jsonArray = new JSONArray(str);
        Object obj = jsonArray.query("/0");
        assertTrue("hello world!".equals(obj), "Expected 'hello world!'");
        obj = jsonArray.query("/1/1");
        assertTrue(Integer.valueOf(1).equals(obj), "Expected 1");
        obj = jsonArray.query("/2/b");
        assertTrue("bVal".equals(obj), "Expected bVal");
        try {
            obj = jsonArray.query("/a/b/c");
            assertTrue(false, "Expected JSONPointerException");
        } catch (JSONPointerException e) {
            assertTrue("a is not an array index".equals(e.getMessage()), "Expected bad index exception");
        }
    }

    /**
     * Coverage for JSONArray query(JSONPointer)
     */
    @Test
    public void queryFromJSONArrayUsingPointer() {
        String str = "[" +
                "\"hello world!\"," +
                "[0,1,2]," +
                "{" +
                "\"a\":\"aVal\"," +
                "\"b\":\"bVal\"" +
                "}" +
                "]";
        JSONArray jsonArray = new JSONArray(str);
        Object obj = jsonArray.query(new JSONPointer("/0"));
        assertTrue("hello world!".equals(obj), "Expected 'hello world!'");
        obj = jsonArray.query(new JSONPointer("/1/1"));
        assertTrue(Integer.valueOf(1).equals(obj), "Expected 1");
        obj = jsonArray.query(new JSONPointer("/2/b"));
        assertTrue("bVal".equals(obj), "Expected bVal");
        try {
            obj = jsonArray.query(new JSONPointer("/a/b/c"));
            assertTrue(false, "Expected JSONPointerException");
        } catch (JSONPointerException e) {
            assertTrue("a is not an array index".equals(e.getMessage()), "Expected bad index exception");
        }
    }

    /**
     * Coverage for JSONArray optQuery(JSONPointer)
     */
    @Test
    public void optQueryFromJSONArrayUsingPointer() {
        String str = "[" +
                "\"hello world!\"," +
                "[0,1,2]," +
                "{" +
                "\"a\":\"aVal\"," +
                "\"b\":\"bVal\"" +
                "}" +
                "]";
        JSONArray jsonArray = new JSONArray(str);
        Object obj = jsonArray.optQuery(new JSONPointer("/0"));
        assertTrue("hello world!".equals(obj), "Expected 'hello world!'");
        obj = jsonArray.optQuery(new JSONPointer("/1/1"));
        assertTrue(Integer.valueOf(1).equals(obj), "Expected 1");
        obj = jsonArray.optQuery(new JSONPointer("/2/b"));
        assertTrue("bVal".equals(obj), "Expected bVal");
        obj = jsonArray.optQuery(new JSONPointer("/a/b/c"));
        assertTrue(obj == null, "Expected null");
    }
}
