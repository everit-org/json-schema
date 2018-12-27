package org.everit.json.schema;

import static java.util.Collections.emptyList;

import java.net.URI;
import java.util.ArrayList;
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
        List<String> newPointer = new ArrayList<>(pointerToLocation.size() + 1);
        newPointer.addAll(pointerToLocation);
        newPointer.add(key);
        return new SchemaLocation(rootDocumentURI, newPointer);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (rootDocumentURI != null) {
            buffer.append(rootDocumentURI.toString());
        }
        if (buffer.length() == 0 || (buffer.charAt(buffer.length() - 1) != '#' && !(pointerToLocation.isEmpty()))) {
            buffer.append("#");
        }
        pointerToLocation.stream()
                .map(JSONPointer::escape)
                .forEach(e -> buffer.append("/").append(e));
        return buffer.toString();
    }

}
