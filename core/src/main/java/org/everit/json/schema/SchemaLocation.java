package org.everit.json.schema;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SchemaLocation {

    public static final SchemaLocation empty() {
        return new SchemaLocation(null, emptyList());
    }

    public static final SchemaLocation parseURI(String uri) {
        try {
            int hashmarkIdx = uri.indexOf("#");
            if (hashmarkIdx > -1) {
                String rootDocumentURI;
                String rawPointer;
                if (hashmarkIdx == uri.length() - 1) {
                    rootDocumentURI = uri;
                    rawPointer = "";
                } else {
                    rootDocumentURI = uri.substring(0, hashmarkIdx);
                    rawPointer = uri.substring(hashmarkIdx + 1);
                }
                URI documentURI = "".equals(rootDocumentURI) ? null : new URI(rootDocumentURI);
                return new SchemaLocation(documentURI, new JSONPointer(rawPointer).getRefTokens());
            } else {
                return new SchemaLocation(new URI(uri), emptyList());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final URI rootDocumentURI;

    private final List<String> pointerToLocation;

    public SchemaLocation(URI rootDocumentURI, List<String> pointerToLocation) {
        this.rootDocumentURI = rootDocumentURI;
        this.pointerToLocation = new ArrayList<>(requireNonNull(pointerToLocation, "pointerToLocation cannot be null"));
    }

    public SchemaLocation(List<String> pointerToLocation) {
        rootDocumentURI = null;
        this.pointerToLocation = pointerToLocation;
    }

    public SchemaLocation addPointerSegment(String key) {
        List<String> newPointer = new ArrayList<>(pointerToLocation.size() + 1);
        newPointer.addAll(pointerToLocation);
        newPointer.add(key);
        return new SchemaLocation(rootDocumentURI, newPointer);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SchemaLocation))
            return false;
        SchemaLocation that = (SchemaLocation) o;
        return Objects.equals(rootDocumentURI, that.rootDocumentURI) &&
                pointerToLocation.equals(that.pointerToLocation);
    }

    @Override public int hashCode() {
        return Objects.hash(rootDocumentURI, pointerToLocation);
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
