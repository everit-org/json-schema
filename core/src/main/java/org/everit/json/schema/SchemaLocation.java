package org.everit.json.schema;

import static java.util.Collections.emptyList;

import java.net.URI;
import java.util.List;

public class SchemaLocation {

    public static final SchemaLocation empty() {
        return new SchemaLocation(null, emptyList());
    }

    private URI rootDocumentURI;

    private List<String> pointerToLocation;

    public SchemaLocation(URI rootDocumentURI, List<String> pointerToLocation) {
        this.rootDocumentURI = rootDocumentURI;
        this.pointerToLocation = pointerToLocation;
    }

    public SchemaLocation(List<String> pointerToLocation) {
        this.pointerToLocation = pointerToLocation;
    }

    public SchemaLocation addPointerSegment(String key) {
        throw new UnsupportedOperationException();
    }

}
