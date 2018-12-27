package org.everit.json.schema;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

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

    @Test(expected = NullPointerException.class)
    public void parseURI_null() {
        SchemaLocation.parseURI(null);
    }

    @Test
    public void parseURI_noHashmark() {
        SchemaLocation actual = SchemaLocation.parseURI("http://example.org");
        assertEquals(new SchemaLocation(uri("http://example.org"), emptyList()), actual);
    }

    @Test
    public void parseURI_emptyFragment() {
        SchemaLocation actual = SchemaLocation.parseURI("http://example.org#");
        assertEquals(new SchemaLocation(uri("http://example.org#"), emptyList()), actual);
    }

    @Test
    public void parseURI_singleSegmentPointer() {
        SchemaLocation actual = SchemaLocation.parseURI("http://example.org#/key");
        SchemaLocation expected = new SchemaLocation(uri("http://example.org"), new ArrayList<>(asList("key")));
        assertEquals(expected, actual);
    }

    @Test
    public void parseURI_onlyPointer() {
        SchemaLocation actual = SchemaLocation.parseURI("#/key");
        SchemaLocation expected = new SchemaLocation(null, new ArrayList<>(asList("key")));
        assertEquals(expected, actual);
    }

    @Test
    public void parseURI_multiSegmentPointer() {
        SchemaLocation actual = SchemaLocation.parseURI("http://example.org#/key1/key2");
        assertEquals(new SchemaLocation(uri("http://example.org"), asList("key1", "key2")), actual);
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(SchemaLocation.class)
                .withRedefinedSuperclass()
                .withNonnullFields("pointerToLocation")
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

}
