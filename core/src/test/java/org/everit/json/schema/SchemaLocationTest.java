package org.everit.json.schema;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class SchemaLocationTest {

    private static URI uri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void addPointerSegment() {
        SchemaLocation underTest = SchemaLocation.empty();
        SchemaLocation actual = underTest.addPointerSegment("key");
        assertEquals("#/key", actual.toString());
        assertEquals("#", underTest.toString());
    }

    @Test
    public void toString_noURI() {
        SchemaLocation underTest = new SchemaLocation(null, asList("a", "b/b", "c~c"));
        assertEquals("#/a/b~1b/c~0c", underTest.toString());
    }

    @Test
    public void toString_noURI_noPointer() {
        assertEquals("#", SchemaLocation.empty().toString());
    }

    @Test
    public void toString_uriWithTrailingHashmark() {
        SchemaLocation underTest = new SchemaLocation(uri("http://example.org/asd#"), singletonList("key"));
        assertEquals("http://example.org/asd#/key", underTest.toString());
    }

    @Test
    public void toString_uriAndPointer() {
        SchemaLocation underTest = new SchemaLocation(uri("http://example.com/hello"), singletonList("key"));
        assertEquals("http://example.com/hello#/key", underTest.toString());
    }

    @Test
    public void toString_uri_noPointer() {
        SchemaLocation underTest = new SchemaLocation(uri("http://example.com/hello"), emptyList());
        assertEquals("http://example.com/hello", underTest.toString());
    }

}
